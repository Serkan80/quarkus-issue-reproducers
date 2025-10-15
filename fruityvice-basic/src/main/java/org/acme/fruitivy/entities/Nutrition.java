package org.acme.fruitivy.entities;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Embeddable
public record Nutrition(
        @Min(0) @Max(1000) Integer calories,
        @Min(0) @Max(1000) Float fat,
        @Min(0) @Max(1000) Float sugar,
        @Min(0) @Max(1000) Float carbohydrates,
        @Min(0) @Max(1000) Float protein
) {
}
