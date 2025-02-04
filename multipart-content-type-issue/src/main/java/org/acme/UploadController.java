package org.acme;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Path("/upload")
public class UploadController {

    @POST
    @Consumes(MULTIPART_FORM_DATA)
    public RestResponse<Void> upload(@RestForm FileUpload file) {
        return RestResponse.ok();
    }
}
