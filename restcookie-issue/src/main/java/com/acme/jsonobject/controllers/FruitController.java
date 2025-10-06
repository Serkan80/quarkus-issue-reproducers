package com.acme.jsonobject.controllers;

import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/fruits")
public class FruitController {

    @POST
    public RestResponse<String> multipleCookie(@CookieParam("value2") @NotBlank String value2) {
        return RestResponse.ok(value2);
    }
}
