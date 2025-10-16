package com.acme.gateway.rest.dto;

import com.acme.gateway.entities.ApiCredentialEntity;
import com.acme.gateway.entities.CompositeApiId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import static io.quarkus.runtime.util.StringUtil.isNullOrEmpty;

public record ApiCredential(
        @Min(1) @NotNull
        Long subscriptionId,
        String username,
        String password,
        String clientId,
        String clientSecret,
        String apiKey
) {

    @JsonIgnore
    @AssertTrue(message = "no credentials were provided")
    public boolean credentialsArePresent() {
        return !(isNullOrEmpty(this.apiKey)
                 && (isNullOrEmpty(this.clientId) || isNullOrEmpty(this.clientSecret))
                 && (isNullOrEmpty(this.username) || isNullOrEmpty(this.password)));
    }

    public ApiCredentialEntity toEntity() {
        var result = new ApiCredentialEntity();
        result.id = new CompositeApiId();
        result.id.subscriptionId = this.subscriptionId;
        result.username = this.username;
        result.password = this.password;
        result.clientId = this.clientId;
        result.clientSecret = this.clientSecret;
        result.apiKey = this.apiKey;
        return result;
    }
}
