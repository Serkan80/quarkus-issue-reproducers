package org.acme.fruitconsumer.rest;

import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.acme.fruitconsumer.entities.FruitEntity;
import org.acme.fruitconsumer.rest.dto.Fruit;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestStreamElementType;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/fruits")
public class FruitController {

    @Inject
    @Channel("fruit-in")
    Multi<Fruit> fruits;

    @GET
    @RestStreamElementType(APPLICATION_JSON)
    public Multi<Fruit> stream() {
        return this.fruits;
    }

    @GET
    @Path("/{name}")
    public Fruit findByName(@RestPath String name) {
        return FruitEntity.findByName(name);
    }

    @GET
    public List<Fruit> findAll() {
        return FruitEntity.allFruits();
    }
}
