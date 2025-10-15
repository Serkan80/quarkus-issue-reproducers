package org.acme.fruitivy.clients;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.acme.fruitivy.rest.dto.Fruit;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;

import java.time.temporal.ValueRange;
import java.util.Optional;

@Path("/api/fruit")
@RegisterRestClient(configKey = "fruitivy")
public interface FruitivyDirectClient {

    @GET
    @Path("/{name}")
    RestResponse<Fruit> findByName(String name);

    default Fruit findOptionally(String name) {
        return Optional.ofNullable(findByName(name))
                       .filter(res -> ValueRange.of(200, 204).isValidValue(res.getStatus()))
                       .map(RestResponse::getEntity)
                       .filter(fruit -> fruit.nutrition() != null)
                       .orElseThrow(() -> new ClientWebApplicationException("Fruit(name=%s) nutrition not found".formatted(name)));
    }
}
