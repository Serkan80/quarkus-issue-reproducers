package com.acme.jsonobject.controllers;

import io.vertx.core.json.JsonObject;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

public class CreatePayloadRequest {

    @NotNull
    public JsonObject payload;
}
