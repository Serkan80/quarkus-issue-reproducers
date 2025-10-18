package org.acme.fruitproducer.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Fruit(

        @NotBlank
        @Size(min = 3, max = 50)
        String name,

        @NotBlank
        @Size(min = 3, max = 50)
        String family,

        Nutritions nutritions
) {

    public Fruit normalize() {
        return new Fruit(this.name.toLowerCase(), this.family.toLowerCase(), this.nutritions);
    }
}
