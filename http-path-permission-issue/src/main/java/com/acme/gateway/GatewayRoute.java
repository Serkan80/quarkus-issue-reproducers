package com.acme.gateway;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import org.apache.camel.Exchange;
import org.apache.camel.attachment.AttachmentMessage;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;

import static com.acme.gateway.CamelUtils.setErrorMessage;
import static io.quarkus.runtime.util.StringUtil.isNullOrEmpty;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.EXCEPTION_CAUGHT;
import static org.apache.camel.Exchange.HTTP_PATH;
import static org.apache.camel.Exchange.HTTP_URI;
import static org.apache.camel.LoggingLevel.DEBUG;
import static org.apache.camel.component.platform.http.vertx.VertxPlatformHttpConstants.REMOTE_ADDRESS;
import static org.apache.hc.core5.http.ContentType.MULTIPART_FORM_DATA;


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
                .log(DEBUG, "incoming: ${headers}")
                .process(CamelUtils::validateUser)
                .choice()
                    .when(header(CONTENT_TYPE).contains(MULTIPART_FORM_DATA.getMimeType()))
                        .process(this::multiPartProcessor)
                .end()
                .process(this::forwardUrlProcessor)
                .removeHeader(HTTP_PATH)
                .removeHeader(HTTP_URI)
                .log(DEBUG, "forwarding url: ${exchangeProperty.forwardUrl}")
                .toD("${exchangeProperty.forwardUrl}?bridgeEndpoint=true&skipRequestHeaders=true&followRedirects=true&connectionClose=true&copyHeaders=true");
        //@formatter:on
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
            attachments.entrySet().forEach(entry -> multiPartBuilder.addBinaryBody("file", Unchecked.supplier(() -> entry.getValue().getInputStream()).get()));
        }

        exchange.getMessage().setBody(multiPartBuilder.build());
    }

    private void forwardUrlProcessor(Exchange exchange) {
        var incomingRequestPath = exchange.getIn().getHeader(HTTP_URI, String.class);
        var proxyIndexStart = incomingRequestPath.indexOf('/', 1);
        var proxyIndexEnd = incomingRequestPath.indexOf('/', proxyIndexStart + 1);

        if (proxyIndexEnd == -1) {
            proxyIndexEnd = incomingRequestPath.length();
        }

        var proxyName = incomingRequestPath.substring(proxyIndexStart, proxyIndexEnd);
        var proxyPath = incomingRequestPath.substring(proxyIndexEnd);
        var proxyUrl = this.routes.get(proxyName.substring(1));
        Log.debugf("proxyName: %s, proxyUrl: %s, proxyPath: %s\n", proxyName, proxyUrl, proxyPath);

        if (isNullOrEmpty(proxyUrl)) {
            throw new NotFoundException("No proxy found for request: %s".formatted(incomingRequestPath));
        }

        exchange.setProperty("forwardUrl", "%s%s".formatted(proxyUrl, proxyPath));
        exchange.getIn().setHeader("X-Forward-For", exchange.getIn().getHeader(REMOTE_ADDRESS));
    }
}
