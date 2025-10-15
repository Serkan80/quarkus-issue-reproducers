package org.acme.fruitivy.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.resteasy.reactive.jackson.SecureField;
import org.acme.fruitivy.entities.FruitEntity;
import org.acme.fruitivy.entities.Nutrition;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Fruit(
        @SecureField(rolesAllowed = "admin")
        Long id,
        String name,
        String family,
        @JsonProperty("nutritions")
        Nutrition nutrition
) {
    public static Fruit toDto(FruitEntity entity) {
        return new Fruit(entity.id, entity.name, entity.family, entity.nutrition);
    }
}
