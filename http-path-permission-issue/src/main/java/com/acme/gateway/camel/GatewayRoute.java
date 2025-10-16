package com.acme.gateway.camel;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import static com.acme.gateway.camel.SubscriptionProcessor.SUBSCRIPTION_KEY;
import static com.acme.gateway.camel.SubscriptionProcessor.THROTTLING_ENABLED;
import static com.acme.gateway.camel.SubscriptionProcessor.THROTTLING_MAX_REQUESTS;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.hc.core5.http.ContentType.MULTIPART_FORM_DATA;

@ApplicationScoped
public class GatewayRoute extends EndpointRouteBuilder {

    @Inject
    SubscriptionProcessor subscriptionProcessor;

    @Override
    public void configure() {
        onException(Throwable.class)
                .handled(true)
                .process(CamelUtils::cleanHeaders)
                .process(CamelUtils::setErrorMessage)
                .end();

        //@formatter:off
        from(platformHttp("/gateway").matchOnUriPrefix(true))
                .id("gatewayRoute")
                .to("micrometer:timer:gateway-metrics?action=start")
                .process(CamelUtils::validateUser)
                .process(this.subscriptionProcessor)
                .choice()
                    .when(exchangeProperty(THROTTLING_ENABLED).isEqualTo(true))
                        .to("direct:throttling")
                .end()
                .choice()
                    .when(header(CONTENT_TYPE).contains(MULTIPART_FORM_DATA.getMimeType()))
                        .process(CamelUtils::multiPartProcessor)
                .end()
                .process(CamelUtils::forwardUrlProcessor)
                .process(CamelUtils::cleanHeaders)
                .toD("${exchangeProperty.forwardUrl}?bridgeEndpoint=true&skipRequestHeaders=true&followRedirects=true&connectionClose=true&copyHeaders=true")
                .to("micrometer:timer:gateway-metrics?action=stop");
        //@formatter:on

        from("direct:throttling")
                .throttle(exchangeProperty(THROTTLING_MAX_REQUESTS), header(SUBSCRIPTION_KEY))
                .totalRequestsMode()
                .rejectExecution(true)
                .timePeriodMillis(60000);
    }
}

