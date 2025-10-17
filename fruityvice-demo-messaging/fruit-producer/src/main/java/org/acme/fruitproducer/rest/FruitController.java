package org.acme.fruitproducer.rest;

import io.quarkus.logging.Log;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.acme.fruitproducer.clients.FruityViceClient;
import org.acme.fruitproducer.rest.dto.Fruit;
import org.acme.fruitproducer.rest.dto.ValidationGroups.Post;
import org.acme.fruitproducer.rest.dto.Vote;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestPath;

@Path("/fruits")
public class FruitController {

    @Inject
    @Channel("fruit-out")
    Emitter<Fruit> fruitEmitter;

    @Inject
    @Channel("vote-out")
    Emitter<Vote> voteEmitter;

    @RestClient
    FruityViceClient client;

    @POST
    @RolesAllowed("admin")
    public void sendFruit(@Valid @ConvertGroup(to = Post.class) Fruit fruit) {
        var normalized = fruit.normalize();
        this.fruitEmitter.send(normalized);
        Log.infof("New %s sent", normalized);
    }

    @PATCH
    @Path("/{name}")
    public void updateFruitRemotely(@RestPath String name) {
        var fruit = this.client.findOptionally(name).normalize();
        this.fruitEmitter.send(fruit);
        Log.infof("%s update sent", fruit);
    }

    @POST
    @Path("/votes")
    public void sendVote(@Valid Vote vote) {
        this.voteEmitter.send(vote);
        Log.infof("%s sent", vote);
    }
}
