package com.acme.gateway.rest.dto;

import com.acme.gateway.entities.SubscriptionEntity;

import java.time.OffsetDateTime;
import java.util.List;

public record SubscriptionAll(
        String subscriptionKey,
        String subject,
        boolean enabled,
        OffsetDateTime createdAt,
        List<Api> apis
) {
    public static SubscriptionAll toDto(SubscriptionEntity entity) {
        var apis = entity.apis.stream().map(Api::toDto).toList();
        return new SubscriptionAll(
                entity.subscriptionKey,
                entity.subject,
                entity.enabled,
                entity.createdAt,
                apis);
    }
}
