package com.acme.gateway.rest.dto;

import com.acme.gateway.entities.SubscriptionEntity;

import java.time.OffsetDateTime;

public record Subscription(
        String subscriptionKey,
        String subject,
        boolean enabled,
        OffsetDateTime createdAt
) {
    public static Subscription toDto(SubscriptionEntity entity) {
        return new Subscription(entity.subscriptionKey, entity.subject, entity.enabled, entity.createdAt);
    }
}
