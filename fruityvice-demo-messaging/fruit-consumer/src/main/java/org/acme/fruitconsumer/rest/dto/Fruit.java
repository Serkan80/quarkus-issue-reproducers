package org.acme.fruitconsumer.rest.dto;

public record Fruit(String name, String family, Nutritions nutritions, Long counter) {

    public Fruit normalize() {
        return new Fruit(this.name.toLowerCase(), this.family.toLowerCase(), this.nutritions, this.counter);
    }
}

