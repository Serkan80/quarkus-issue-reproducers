package org.acme.fruitproducer.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.acme.fruitproducer.rest.dto.ValidationGroups.Post;

public record Fruit(

        @NotBlank(groups = Post.class)
        @Size(min = 3, max = 50, groups = Post.class)
        String name,

        @NotBlank(groups = Post.class)
        @Size(min = 3, max = 50, groups = Post.class)
        String family,

        Nutritions nutritions
) {

    public Fruit normalize() {
        return new Fruit(this.name.toLowerCase(), this.family.toLowerCase(), this.nutritions);
    }
}
