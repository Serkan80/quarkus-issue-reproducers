package org.acme.fruitconsumer.rest;

import io.quarkus.logging.Log;
import jakarta.ws.rs.WebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.util.Map;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static java.util.Objects.requireNonNullElse;

public class ExceptionMapper {

    @ServerExceptionMapper(priority = 1)
    public RestResponse<Map<String, Object>> toResponse(WebApplicationException e) {
        var status = requireNonNullElse(e.getResponse().getStatusInfo(), BAD_REQUEST);
        var message = getMessage(e);
        Log.error(message);

        return RestResponse.status(
                status, Map.of(
                        "message", message,
                        "status", status.getStatusCode(),
                        "exception", e.getClass().getName()
                ));
    }

    @ServerExceptionMapper(priority = 2)
    public RestResponse<Map<String, Object>> toResponse(Exception e) {
        var message = requireNonNullElse(getMessage(e), "No error message was provided");
        Log.error(message);

        return RestResponse.status(
                INTERNAL_SERVER_ERROR, Map.of(
                        "message", message,
                        "status", 500,
                        "exception", e.getClass().getName()
                ));
    }

    private String getMessage(Exception e) {
        var result = e.getMessage();
        var cause = e.getCause();
        var maxDepth = 3;
        while (cause != null && maxDepth-- > 0) {
            result = cause.getMessage();
            cause = cause.getCause();
        }
        return result;
    }
}
