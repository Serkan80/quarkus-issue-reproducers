package org.acme.fruitivy.rest.dto;

import io.quarkus.panache.common.Sort;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.DefaultValue;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.jboss.resteasy.reactive.RestQuery;

public record SearchFilter(
        @Parameter(description = "the start id")
        @RestQuery @DefaultValue("1") @Min(1) int start,

        @Parameter(description = "the max amount of fruits returned")
        @RestQuery @DefaultValue("50") @Min(1) @Max(200) int max,

        @Parameter(description = "isNext=true will search forward (default) from the start index, otherwise it will search backwards")
        @RestQuery @DefaultValue("true") boolean isNext
) {

    public String getQuery() {
        var result = "id >= ?1";
        if (!isNext) {
            result = "id <= ?1";
        }
        return result;
    }

    public Sort getSortOrder() {
        return isNext ? Sort.ascending("id") : Sort.descending("id");
    }
}
