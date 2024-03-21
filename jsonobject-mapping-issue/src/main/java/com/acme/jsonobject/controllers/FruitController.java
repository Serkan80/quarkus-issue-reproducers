package com.acme.jsonobject.controllers;

import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.Map;

@Path("/fruits")
public class FruitController {

    @POST
    @Path("/jsonobject")
    public RestResponse<Map<String, Object>> createOpgave(@Valid CreatePayloadRequest request) {
        return RestResponse.ok(request.payload.getMap());
    }

    @POST
    @Path("/map")
    public RestResponse<Map<String, Object>> createOpgave(@Valid CreatePayloadRequest2 request) {
        return RestResponse.ok(request.payload);
    }
}
