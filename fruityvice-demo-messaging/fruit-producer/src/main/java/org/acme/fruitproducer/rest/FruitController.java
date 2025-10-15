package org.acme.fruitproducer.rest;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.acme.fruitproducer.clients.FruityViceClient;
import org.acme.fruitproducer.rest.dto.Fruit;
import org.acme.fruitproducer.rest.dto.ValidationGroups.Post;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestPath;

@Path("/fruits")
public class FruitController {

    @Inject
    @Channel("fruit-out")
    Emitter<Fruit> emitter;

    @RestClient
    FruityViceClient client;

    @POST
    public void sendFruit(@Valid @ConvertGroup(to = Post.class) Fruit fruit) {
        var normalized = fruit.normalize();
        this.emitter.send(normalized);
        Log.infof("New %s sent", normalized);
    }

    @PATCH
    @Path("/{name}")
    public void updateRemotely(@RestPath String name) {
        var fruit = this.client.findOptionally(name).normalize();
        this.emitter.send(fruit);
        Log.infof("%s update sent", fruit);
    }
}
