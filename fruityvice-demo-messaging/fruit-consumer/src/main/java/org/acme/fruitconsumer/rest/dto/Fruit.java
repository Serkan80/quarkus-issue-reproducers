package org.acme.fruitconsumer.rest.dto;

import static java.util.Locale.ENGLISH;

public record Fruit(Long id, String name, String family, Nutritions nutritions) {

    public Fruit normalize() {
        return new Fruit(this.id, this.name.toLowerCase(ENGLISH), this.family.toLowerCase(ENGLISH), this.nutritions);
    }
}

