package org.acme.fruitivy.rest.dto;

import jakarta.validation.constraints.NotBlank;
import org.acme.fruitivy.entities.FruitEntity;
import org.hibernate.validator.constraints.Length;

public record FruitPOST(

        @NotBlank
        @Length(min = 3, max = 50)
        String name,

        @NotBlank
        @Length(min = 3, max = 50)
        String family

) {
    public FruitEntity toEntity() {
        var result = new FruitEntity();
        result.name = this.name.toLowerCase();
        result.family = this.family.toLowerCase();
        return result;
    }
}
