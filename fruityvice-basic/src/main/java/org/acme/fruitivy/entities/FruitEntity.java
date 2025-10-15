package org.acme.fruitivy.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Optional;

@Entity
@DynamicUpdate
@Table(name = "fruits")
public class FruitEntity extends PanacheEntity {

    @NotBlank
    @Column(unique = true)
    public String name;

    @NotBlank
    public String family;

    @Embedded
    public Nutrition nutrition;

    public static Optional<FruitEntity> findByName(String name) {
        return find("name = ?1", name.toLowerCase()).firstResultOptional();
    }
}
