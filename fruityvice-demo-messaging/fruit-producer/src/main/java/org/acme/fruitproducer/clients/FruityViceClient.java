package org.acme.fruitproducer.clients;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.acme.fruitproducer.rest.dto.Fruit;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestPath;

@Path("/api/fruit")
@RegisterRestClient(configKey = "fruityvice")
public interface FruityViceClient {

    @GET
    @Path("/{name}")
    Uni<Fruit> findByName(@RestPath String name);
}
