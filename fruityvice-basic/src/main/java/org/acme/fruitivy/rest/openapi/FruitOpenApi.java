package org.acme.fruitivy.rest.openapi;

import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.acme.fruitivy.rest.dto.Fruit;
import org.acme.fruitivy.rest.dto.FruitPOST;
import org.acme.fruitivy.rest.dto.SearchFilter;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.hibernate.validator.constraints.Length;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;

@Path("/fruits")
public interface FruitOpenApi {

    @GET
    @Path("/export")
    @Produces("text/csv")
    @Operation(summary = "exports fruits into a csv file")
    Response export();

    @POST
    @Operation(summary = "saves a fruit without its nutritions")
    RestResponse<Void> save(@Valid FruitPOST fruit);

    @PATCH
    @Path("/{name}")
    @Operation(summary = "retrieves and saves the nutritions for a given fruit")
    Fruit fetchNutrition(@RestPath @Length(min = 3, max = 50) String name);

    @GET
    @Operation(summary = "returns all fruits")
    List<Fruit> findAll(@BeanParam @Valid SearchFilter searchFilter);
}
