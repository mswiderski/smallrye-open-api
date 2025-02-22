package io.smallrye.openapi.tck;

import static io.smallrye.openapi.runtime.io.Format.JSON;
import static io.smallrye.openapi.runtime.io.Format.YAML;
import static io.smallrye.openapi.runtime.io.OpenApiSerializer.serialize;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.models.OpenAPI;

import io.smallrye.openapi.runtime.io.Format;

@Path("/openapi")
public class OpenApiEndpoint {
    @Context
    ServletContext servletContext;
    @Context
    HttpHeaders httpHeaders;

    OpenAPI openAPI;

    @PostConstruct
    public void init() {
        this.openAPI = (OpenAPI) servletContext.getAttribute("OpenAPI");
    }

    @GET
    public Response openApi(@QueryParam("format") final String format) throws Exception {
        final Format formatOpenApi = getOpenApiFormat(httpHeaders, format);
        return Response.ok(serialize(openAPI, formatOpenApi).getBytes(UTF_8))
                .type(formatOpenApi.getMimeType())
                .build();
    }

    private Format getOpenApiFormat(final HttpHeaders httpHeaders, final String format) {
        return Stream.of(Format.values())
                .filter(f -> format != null && f.name().compareToIgnoreCase(format) == 0)
                .findFirst()
                .orElse(httpHeaders.getAcceptableMediaTypes().contains(APPLICATION_JSON_TYPE) ? JSON : YAML);
    }
}
