package org.acme.fruitconsumer.rest.dto;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Embeddable
public record Nutritions(
        @Min(0) @Max(1000) Integer calories,
        @Min(0) @Max(1000) Float fat,
        @Min(0) @Max(1000) Float protein,
        @Min(0) @Max(1000) Float sugar,
        @Min(0) @Max(1000) Float carbohydrates
) {
}
