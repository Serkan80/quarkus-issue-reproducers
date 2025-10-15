package org.acme.fruitproducer.rest.dto;

public record Nutritions(
        Integer calories,
        Float fat,
        Float protein,
        Float sugar,
        Float carbohydrates
) {
}
