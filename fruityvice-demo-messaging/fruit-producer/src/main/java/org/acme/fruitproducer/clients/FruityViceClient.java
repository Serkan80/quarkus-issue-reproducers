package org.acme.fruitproducer.clients;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.acme.fruitproducer.rest.dto.Fruit;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;

import java.time.temporal.ValueRange;
import java.util.Optional;

@Path("/api/fruit")
@RegisterRestClient(configKey = "fruityvice")
public interface FruityViceClient {

    @GET
    @Path("/{name}")
    RestResponse<Fruit> findByName(@RestPath String name);

    default Fruit findOptionally(String name) {
        return Optional.ofNullable(findByName(name))
                       .filter(res -> ValueRange.of(200, 204).isValidIntValue(res.getStatus()))
                       .map(RestResponse::getEntity)
                       .filter(fruit -> fruit.nutritions() != null)
                       .orElseThrow(() -> new ClientWebApplicationException("Fruit(name=%s) nutritions could not be retrieved".formatted(name)));
    }
}
