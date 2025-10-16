package org.acme.fruitconsumer.rest.dto;

public record Fruit(Long id, String name, String family, Nutritions nutritions) {

    public Fruit normalize() {
        return new Fruit(this.id, this.name.toLowerCase(), this.family.toLowerCase(), this.nutritions);
    }
}

