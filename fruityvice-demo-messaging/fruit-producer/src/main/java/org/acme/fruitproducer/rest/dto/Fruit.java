package org.acme.fruitproducer.rest.dto;

import io.quarkus.resteasy.reactive.jackson.SecureField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.acme.fruitproducer.rest.dto.ValidationGroups.Post;

public record Fruit(

        @NotBlank(groups = Post.class)
        @Size(min = 3, max = 50, groups = Post.class)
        String name,

        @NotBlank(groups = Post.class)
        @Size(min = 3, max = 50, groups = Post.class)
        @SecureField(rolesAllowed = "admin")
        String family,

        Nutritions nutritions
) {

    public Fruit normalize() {
        return new Fruit(this.name.toLowerCase(), this.family.toLowerCase(), this.nutritions);
    }
}
