package com.acme.gateway;

import io.quarkus.logging.Log;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.apache.camel.attachment.AttachmentMessage;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;

import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_PATH;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.Exchange.HTTP_URI;
import static org.apache.camel.component.platform.http.vertx.VertxPlatformHttpConstants.AUTHENTICATED_USER;
import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA;


@ApplicationScoped
public class GatewayRoute extends EndpointRouteBuilder {

    @ConfigProperty(name = "routes")
    Map<String, String> routes;

    @Override
    public void configure() {
        //@formatter:off
        from(platformHttp("/gateway").matchOnUriPrefix(true).useStreaming(true).advanced().fileNameExtWhitelist("pdf"))
                .id("gatewayRoute")
                .log("incoming: ${headers}")
                .process(this::validateUser)
                .choice()
                    .when(header(CONTENT_TYPE).contains(MULTIPART_FORM_DATA.getMimeType()))
                        .process(this::multiPartProcessor)
                .end()
                .process(this::forwardUrlProcessor)
                .removeHeaders(HTTP_PATH)
                .removeHeader(HTTP_URI)
                .log("forwarding url: ${header.forwardUrl}")
                .toD("${header.forwardUrl}?bridgeEndpoint=true&skipRequestHeaders=true&followRedirects=true&connectionClose=true&copyHeaders=false");
        //@formatter:on
    }

    private void validateUser(Exchange exchange) {
        var user = exchange.getIn().getHeader(AUTHENTICATED_USER, QuarkusHttpUser.class);
        if (user == null || user.principal().getString("username").isBlank()) {
            setErrorMessage(exchange, 401, "User not authenticated");
        }
    }

    private void multiPartProcessor(Exchange exchange) {
        var body = (Map<String, Object>) exchange.getIn().getBody(Map.class);
        var attachments = exchange.getIn(AttachmentMessage.class).getAttachments();

        var multiPartBuilder = MultipartEntityBuilder.create();
        body.entrySet().forEach(entry -> {
            multiPartBuilder.addTextBody(entry.getKey(), entry.getValue().toString());
            exchange.getIn().getHeaders().remove(entry.getKey());
        });
        attachments.entrySet().forEach(entry -> multiPartBuilder.addBinaryBody(entry.getKey(), Unchecked.supplier(() -> entry.getValue().getInputStream()).get()));

        exchange.getMessage().setBody(multiPartBuilder.build());
    }

    private void forwardUrlProcessor(Exchange exchange) {
        var incomingRequestPath = exchange.getIn().getHeader(HTTP_URI, String.class);
        var serviceIndexStart = incomingRequestPath.indexOf('/', 1);
        var service = incomingRequestPath.substring(serviceIndexStart, incomingRequestPath.indexOf('/', serviceIndexStart + 1));

        var proxyPath = incomingRequestPath.substring(incomingRequestPath.indexOf(service) + service.length() + 1);
        var proxyUrl = this.routes.get(service.substring(1));

        if (proxyUrl == null || proxyUrl.isBlank()) {
            setErrorMessage(exchange, 404, "No proxy found for url: %s".formatted(incomingRequestPath));
        } else {
            Log.debugf("service: %s, proxyUrl: %s, proxyPath: %s\n", service, proxyUrl, proxyPath);
            exchange.getIn().setHeader("forwardUrl", "%s/%s".formatted(proxyUrl, proxyPath));
        }
    }

    private void setErrorMessage(Exchange exchange, int statusCode, String errorMessage) {
        var message = exchange.getMessage();
        message.setHeader(HTTP_RESPONSE_CODE, statusCode);
        message.setHeader(CONTENT_TYPE, constant("text/plain"));
        message.setBody(errorMessage);
        exchange.setRouteStop(true);
    }
}
