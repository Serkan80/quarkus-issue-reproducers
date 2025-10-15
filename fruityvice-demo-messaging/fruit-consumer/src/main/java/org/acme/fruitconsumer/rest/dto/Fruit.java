package org.acme.fruitconsumer.rest.dto;

public record Fruit(String name, String family, Nutritions nutritions) {

    public Fruit normalize() {
        return new Fruit(this.name.toLowerCase(), this.family.toLowerCase(), this.nutritions);
    }
}

