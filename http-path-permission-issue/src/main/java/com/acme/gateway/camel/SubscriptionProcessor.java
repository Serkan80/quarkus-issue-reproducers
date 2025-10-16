package com.acme.gateway.camel;

import com.acme.gateway.entities.SubscriptionEntity;
import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Singleton;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.function.Function;

import static org.apache.camel.Exchange.HTTP_URI;

@Singleton
public class SubscriptionProcessor implements Processor {

    public static final String SUBSCRIPTION_KEY = "subscription-key";
    public static final String SUBSCRIPTION = "subscription";
    public static final String THROTTLING_ENABLED = "throttle_enabled";
    public static final String THROTTLING_MAX_REQUESTS = "throttling_maxRequests";

    @Override
    @ActivateRequestContext
    public void process(Exchange exchange) {
        var in = exchange.getIn();
        var incomingRequest = in.getHeader(HTTP_URI, String.class);
        var subscriptionKey = in.getHeader(SUBSCRIPTION_KEY, String.class);
        var subscription = SubscriptionEntity.findByKey(subscriptionKey);
        var api = subscription.findApiBy(CamelUtils.extractProxyName(incomingRequest).proxyName(), Function.identity());

        if (api == null) {
            throw new UnauthorizedException("Subscriber has no authorization for %s".formatted(incomingRequest));
        }

        if (api.maxRequests != null) {
            exchange.setProperty(THROTTLING_ENABLED, true);
            exchange.setProperty(THROTTLING_MAX_REQUESTS, api.maxRequests);
        }

        in.setHeader(SUBSCRIPTION, subscription);
    }
}
