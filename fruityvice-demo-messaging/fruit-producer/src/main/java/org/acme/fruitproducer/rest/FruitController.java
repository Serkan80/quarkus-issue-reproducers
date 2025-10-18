package org.acme.fruitproducer.rest;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.acme.fruitproducer.clients.FruityViceClient;
import org.acme.fruitproducer.rest.dto.Fruit;
import org.acme.fruitproducer.rest.dto.Vote;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/fruits")
public class FruitController {

    @Inject
    @Channel("fruit-out")
    MutinyEmitter<Fruit> fruitEmitter;

    @Inject
    @Channel("vote-out")
    MutinyEmitter<Vote> voteEmitter;

    @RestClient
    FruityViceClient client;

    @POST
    @Path("/{name}")
    @RolesAllowed("admin")
    public Uni<RestResponse<Fruit>> sendFruit(@RestPath String name) {
        return this.client.findByName(name)
                          .onFailure().transform(e -> new ClientWebApplicationException("Fruit(name=%s) was not found on fruitivy.com".formatted(name)))
                          .map(Fruit::normalize)
                          .call(this.fruitEmitter::send)
                          .invoke(fruit -> Log.infof("%s sent", fruit))
                          .map(RestResponse::ok);
    }

    @POST
    @Path("/votes")
    public Uni<Void> sendVote(@Valid Vote vote) {
        return this.voteEmitter.send(vote).invoke(() -> Log.infof("%s sent", vote));
    }
}
