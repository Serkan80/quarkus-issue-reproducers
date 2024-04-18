package com.acme.gateway;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.jboss.resteasy.reactive.RestForm;

import java.nio.file.Files;
import java.util.Map;

import static com.acme.gateway.CamelUtils.requireNonNullOrElse;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNullElse;

@PermitAll
@Path("/mp")
public class MultipartController {

    @POST
    @Consumes(MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON)
    public Map<String, String> upload(@RestForm java.nio.file.Path file, @RestForm @NotBlank String text) {
        Log.debugf("Received upload, text: %s, file: %s", text, requireNonNullElse(file, "no file uploaded"));
        var content = Unchecked.supplier(() -> Files.readString(file, UTF_8));

        return Map.of(
                "textContent", text,
                "fileContent", requireNonNullOrElse(file, content.get(), "")
        );
    }
}
