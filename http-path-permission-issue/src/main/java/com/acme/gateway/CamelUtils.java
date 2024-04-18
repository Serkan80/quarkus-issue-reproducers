package com.acme.gateway;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.WebApplicationException;
import org.apache.camel.Exchange;
import org.apache.camel.http.base.HttpOperationFailedException;

import static io.quarkus.runtime.util.StringUtil.isNullOrEmpty;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.FAILURE_ENDPOINT;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.ExchangePropertyKey.FAILURE_ROUTE_ID;
import static org.apache.camel.component.platform.http.vertx.VertxPlatformHttpConstants.AUTHENTICATED_USER;

public final class CamelUtils {

    private CamelUtils() {
        super();
    }

    public static void validateUser(Exchange exchange) {
        var user = exchange.getIn().getHeader(AUTHENTICATED_USER, QuarkusHttpUser.class);
        if (user == null || user.principal().getString("username").isBlank()) {
            throw new AuthenticationFailedException("User not authenticated");
        }
    }

    public static void setErrorMessage(Exchange exchange, Exception e) {
        var status = 500;
        var errorMsg = e.getMessage();

        switch (e) {
            case HttpOperationFailedException he -> {
                status = he.getStatusCode();
                errorMsg = he.getResponseBody();
            }
            case WebApplicationException we -> status = we.getResponse().getStatus();
            case AuthenticationFailedException ae -> status = 401;
            default -> status = 500;
        }

        var message = exchange.getMessage();
        message.setHeader(HTTP_RESPONSE_CODE, status);
        message.setHeader(CONTENT_TYPE, APPLICATION_JSON);
        message.setBody(JsonObject.of(
                "routeId", exchange.getProperty(FAILURE_ROUTE_ID),
                "exception", e.getClass(),
                "message", requireNonBlankElse(errorMsg, e.getMessage()),
                "failureEndpoint", trimOptions(exchange.getProperty(FAILURE_ENDPOINT, String.class))
        ).encodePrettily());
        exchange.setRouteStop(true);
    }

    public static String requireNonBlankElse(String original, String orElse) {
        if (isNullOrEmpty(original) || "{}".equals(original)) {
            return orElse;
        }

        return original;
    }

    public static String requireNonNullOrElse(Object condition, String value, String orElse) {
        if (condition == null) {
            return orElse;
        }

        return value;
    }

    public static String trimOptions(String url) {
        var optionsIndex = url.indexOf('?');
        if (optionsIndex == -1) {
            return url;
        }
        return url.substring(0, optionsIndex);
    }
}
