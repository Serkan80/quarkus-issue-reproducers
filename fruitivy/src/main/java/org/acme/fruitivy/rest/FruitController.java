package org.acme.fruitivy.rest;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Page;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.acme.fruitivy.clients.FruitivyDirectClient;
import org.acme.fruitivy.entities.FruitEntity;
import org.acme.fruitivy.rest.dto.Fruit;
import org.acme.fruitivy.rest.dto.FruitPOST;
import org.acme.fruitivy.rest.dto.SearchFilter;
import org.acme.fruitivy.rest.openapi.FruitOpenApi;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.postgresql.copy.CopyManager;
import org.postgresql.jdbc.PgConnection;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static jakarta.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hibernate.jpa.QueryHints.HINT_READONLY;

@ApplicationScoped
public class FruitController implements FruitOpenApi {

    @RestClient
    FruitivyDirectClient client;

    @Override
    @Transactional
    @RolesAllowed("admin")
    public RestResponse<Void> save(FruitPOST fruit) {
        var entity = fruit.toEntity();
        entity.persist();

        Log.infof("%s saved with id=%d", fruit, entity.id);
        return RestResponse.created(URI.create("/fruits/%d".formatted(entity.id)));
    }

    @Override
    @Transactional
    @RolesAllowed("admin")
    public Fruit fetchNutrition(String name) {
        var entity = FruitEntity.findByName(name).orElseThrow(() -> new NotFoundException("Fruit(name=%s) not found".formatted(name)));
        var response = this.client.findOptionally(entity.name);
        entity.nutrition = response.nutrition();

        Log.infof("Fruit(name=%s, nutrition=%s) updated", name, response.nutrition());
        return Fruit.toDto(entity);
    }

    @Override
    public List<Fruit> findAll(SearchFilter searchFilter) {
        return FruitEntity.<FruitEntity>find(searchFilter.getQuery(), searchFilter.getSortOrder(), searchFilter.start())
                          .withHint(HINT_READONLY, true)
                          .page(Page.ofSize(searchFilter.max()))
                          .project(Fruit.class)
                          .list();
    }

    @Override
    public Response export() {
        StreamingOutput output = out -> FruitEntity.getSession().doWork(con -> {
            try {
                new CopyManager(con.unwrap(PgConnection.class)).copyOut(
                        "copy (select * from fruits order by family) to STDOUT (FORMAT csv, HEADER, DELIMITER ';', ENCODING 'UTF-8')",
                        out
                );
            } catch (IOException e) {
                throw new WebApplicationException(e);
            }
        });

        var currentDate = DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now().truncatedTo(SECONDS)).replaceAll("[:-]", "");
        Log.debugf("date = %s", currentDate);

        return Response.ok(output)
                       .header(CONTENT_DISPOSITION, "attachment;filename=fruits-export-%s.csv".formatted(currentDate))
                       .build();
    }
}
