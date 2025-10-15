package org.acme.fruitconsumer.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.NotFoundException;
import org.acme.fruitconsumer.rest.dto.Fruit;
import org.acme.fruitconsumer.rest.dto.Nutritions;

import java.util.List;

import static org.hibernate.jpa.QueryHints.HINT_READONLY;

@Entity
@Table(name = "fruits")
public class FruitEntity extends PanacheEntity {

    @NotBlank
    @Column(unique = true)
    public String name;

    @NotBlank
    public String family;

    @Embedded
    public Nutritions nutritions;

    public static void upsert(Fruit fruit) {
        var normalizedFruit = fruit.normalize();

        //@formatter:off
        getEntityManager().createQuery(
               """
               insert into FruitEntity (name, family, nutritions) values(:name, :family, :nutritions)
               on conflict (name) do
               update set nutritions = excluded.nutritions, family = excluded.family where name = excluded.name
               """
       )
       .setParameter("name", normalizedFruit.name())
       .setParameter("family", normalizedFruit.family())
       .setParameter("nutritions", normalizedFruit.nutritions())
       .executeUpdate();
        //@formatter:on

        Log.infof("%s updated", normalizedFruit);
    }

    public static Fruit findByName(String name) {
        return find("lower(name) = lower(?1)", name)
                .project(Fruit.class)
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException("Fruit(name=%s) not found".formatted(name)));
    }

    public static List<Fruit> allFruits() {
        return findAll(Sort.by("name"))
                .project(Fruit.class)
                .withHint(HINT_READONLY, true)
                .list();
    }
}
