package com.acme.jsonobject.controllers;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class CreatePayloadRequest2 {

    @NotNull
    public Map<String, Object> payload;
}
