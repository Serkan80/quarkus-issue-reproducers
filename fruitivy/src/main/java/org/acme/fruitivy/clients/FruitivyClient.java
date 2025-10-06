package org.acme.fruitivy.clients;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.acme.fruitivy.rest.dto.Fruit;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/")
//@OidcClientFilter
@RegisterRestClient(configKey = "fruitivy")
//@ClientHeaderParam(name = "subscription-key", value = "W6zBCz1SExfV3OEkp9CIb1Pl672M4XPu")
public interface FruitivyClient {

    @GET
    @Path("/{name}")
    Fruit findByName(String name);
}
