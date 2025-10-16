package org.acme.fruitconsumer.rest;

import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.acme.fruitconsumer.entities.FruitEntity;
import org.acme.fruitconsumer.entities.VoteEntity;
import org.acme.fruitconsumer.rest.dto.Fruit;
import org.acme.fruitconsumer.rest.dto.Vote;
import org.acme.fruitconsumer.rest.dto.VoteSummary;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestStreamElementType;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hibernate.jpa.QueryHints.HINT_READONLY;

@Path("/fruits")
public class FruitController {

    @Inject
    @Channel("fruit-in")
    Multi<Fruit> fruits;

    @GET
    public List<Fruit> findAll() {
        return FruitEntity.allFruits();
    }

    @GET
    @Path("/{name}")
    public Fruit findByName(@RestPath String name) {
        return FruitEntity.findByName(name);
    }

    @GET
    @Path("/stream")
    @RestStreamElementType(APPLICATION_JSON)
    public Multi<Fruit> stream() {
        return this.fruits.emitOn(Infrastructure.getDefaultWorkerPool())
                .map(fruit -> FruitEntity.findByName(fruit.name()));
    }

    @GET
    @Path("/votes")
    public List<Vote> votes() {
        return VoteEntity.findAll(Sort.by("fruit"))
                .project(Vote.class)
                .withHint(HINT_READONLY, true)
                .list();
    }

    @GET
    @Path("/votes/summary")
    public List<VoteSummary> voteSummary(@RestQuery("by_channel") @DefaultValue("false") boolean byChannel) {
        return VoteEntity.summary(byChannel);
    }
}
