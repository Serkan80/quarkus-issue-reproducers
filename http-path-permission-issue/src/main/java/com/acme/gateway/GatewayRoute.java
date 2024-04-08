package com.acme.gateway;

import io.quarkus.logging.Log;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import org.apache.camel.Exchange;
import org.apache.camel.attachment.AttachmentMessage;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;
import java.util.Objects;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.EXCEPTION_CAUGHT;
import static org.apache.camel.Exchange.FAILURE_ENDPOINT;
import static org.apache.camel.Exchange.HTTP_PATH;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.Exchange.HTTP_URI;
import static org.apache.camel.ExchangePropertyKey.FAILURE_ROUTE_ID;
import static org.apache.camel.ExchangePropertyKey.TO_ENDPOINT;
import static org.apache.camel.component.platform.http.vertx.VertxPlatformHttpConstants.AUTHENTICATED_USER;
import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA;


@ApplicationScoped
public class GatewayRoute extends EndpointRouteBuilder {

    @ConfigProperty(name = "routes")
    Map<String, String> routes;

    @Override
    public void configure() {
        onException(Throwable.class)
                .handled(true)
                .process(exchange -> setErrorMessage(exchange, exchange.getProperty(EXCEPTION_CAUGHT, Exception.class)))
                .end();

        //@formatter:off
        from(platformHttp("/gateway").matchOnUriPrefix(true))
                .id("gatewayRoute")
                .log("incoming: ${headers}")
                .process(this::validateUser)
                .choice()
                    .when(header(CONTENT_TYPE).contains(MULTIPART_FORM_DATA.getMimeType()))
                        .process(this::multiPartProcessor)
                .end()
                .process(this::forwardUrlProcessor)
                .removeHeader(HTTP_PATH)
                .removeHeader(HTTP_URI)
                .log("forwarding url: ${header.forwardUrl}")
                .toD("${header.forwardUrl}?bridgeEndpoint=true&skipRequestHeaders=true&followRedirects=true&connectionClose=true&copyHeaders=true");
        //@formatter:on
    }

    private void validateUser(Exchange exchange) {
        var user = exchange.getIn().getHeader(AUTHENTICATED_USER, QuarkusHttpUser.class);
        if (user == null || user.principal().getString("username").isBlank()) {
            throw new AuthenticationFailedException("User not authenticated");
        }
    }

    private void multiPartProcessor(Exchange exchange) {
        var body = (Map<String, Object>) exchange.getIn().getBody(Map.class);
        var message = exchange.getIn(AttachmentMessage.class);
        var attachments = message.getAttachments();

        var multiPartBuilder = MultipartEntityBuilder.create();

        // text part of multipart
        if (body != null) {
            body.entrySet().forEach(entry -> {
                multiPartBuilder.addTextBody(entry.getKey(), entry.getValue().toString());
                exchange.getIn().getHeaders().remove(entry.getKey());
            });
        }

        // binary part of multipart
        if (attachments != null) {
            message.getAttachmentObjects().entrySet().forEach(entry -> Log.debugf("attachmentObject key: %s, value: %s", entry.getKey(), entry.getValue().getHeaderNames()));
            attachments.entrySet().forEach(entry -> multiPartBuilder.addBinaryBody(entry.getKey(), Unchecked.supplier(() -> entry.getValue().getInputStream()).get()));

            // line below works, because it sets the correct form-name, but it is hard-coded
            // attachments.entrySet().forEach(entry -> multiPartBuilder.addBinaryBody("file", Unchecked.supplier(() -> entry.getValue().getInputStream()).get()));
        }

        exchange.getMessage().setBody(multiPartBuilder.build());
    }

    private void forwardUrlProcessor(Exchange exchange) {
        var incomingRequestPath = exchange.getIn().getHeader(HTTP_URI, String.class);
        var serviceIndexStart = incomingRequestPath.indexOf('/', 1);
        var serviceIndexEnd = incomingRequestPath.indexOf('/', serviceIndexStart + 1);

        if (serviceIndexEnd == -1) {
            serviceIndexEnd = incomingRequestPath.length();
        }

        var service = incomingRequestPath.substring(serviceIndexStart, serviceIndexEnd);
        var proxyPath = incomingRequestPath.substring(incomingRequestPath.indexOf(service) + service.length());
        var proxyUrl = this.routes.get(service.substring(1));

        if (proxyUrl == null || proxyUrl.isBlank()) {
            throw new NotFoundException("No proxy found for url: %s".formatted(incomingRequestPath));
        } else {
            Log.debugf("service: %s, proxyUrl: %s, proxyPath: %s\n", service, proxyUrl, proxyPath);
            exchange.getIn().setHeader("forwardUrl", "%s%s".formatted(proxyUrl, proxyPath));
        }
    }

    private void setErrorMessage(Exchange exchange, Exception e) {
        var status = 500;
        var errorMsg = e.getMessage();

        switch (e) {
            case HttpOperationFailedException h -> {
                status = h.getStatusCode();
                errorMsg = h.getResponseBody();
            }
            case WebApplicationException we -> status = we.getResponse().getStatus();
            case AuthenticationFailedException ae -> status = 401;
            default -> status = 500;
        }

        var message = exchange.getMessage();
        message.setHeader(HTTP_RESPONSE_CODE, status);
        message.setHeader(CONTENT_TYPE, APPLICATION_JSON);
        message.setBody(JsonObject.of(
                "exception", e.getClass(),
                "message", Objects.requireNonNullElse(errorMsg, e.getMessage()),
                "failureEndpoint", exchange.getProperty(FAILURE_ENDPOINT),
                "routeId", exchange.getProperty(FAILURE_ROUTE_ID),
                "toEndpoint", exchange.getProperty(TO_ENDPOINT)
        ).encodePrettily());
        exchange.setRouteStop(true);
    }
}
