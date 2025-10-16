package com.acme.gateway.entities;

public enum AuthenticationType {
    NONE,
    BASIC,
    CLIENT_CREDENTIALS,
    AUTHORIZATION_FLOW,
    API_KEY,
    PASSTHROUGH
}
