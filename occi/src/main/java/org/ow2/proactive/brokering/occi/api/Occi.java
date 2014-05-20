package org.ow2.proactive.brokering.occi.api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/occi")
@Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
public interface Occi {

    // ************* OCCI SERVER MANAGEMENT **************

    @POST
    @Path("/")
    public Response occiAction(@QueryParam("action") String action);

    @GET
    @Path("/")
    public Response getOcciState(@QueryParam("action") String action);

    // ************* RESOURCE MANAGEMENT **************

    @POST
    @Path("/{category}")
    public javax.ws.rs.core.Response createResource(@HeaderParam("Host") String host, @PathParam("category") String category, @HeaderParam("X-OCCI-Attribute") String attributes);

    @GET
    @Path("/{category}")
    public javax.ws.rs.core.Response getAllResources(@PathParam("category") String category);

    @GET
    @Path("/{category}/{uuid}")
    public javax.ws.rs.core.Response getResource(@PathParam("category") String category, @PathParam("uuid") String uuid, @QueryParam("attribute") String attribute);

    @POST
    @Path("/{category}/{uuid}")
    public javax.ws.rs.core.Response updateResource(@PathParam("category") String category, @PathParam("uuid") String uuid, @QueryParam("action") String action, @HeaderParam("X-OCCI-Attribute") String attributes);

    @PUT
    @Path("/{category}/{uuid}")
    public javax.ws.rs.core.Response fullUpdateResource(@PathParam("category") String category, @PathParam("uuid") String uuid, @QueryParam("action") String action, @HeaderParam("X-OCCI-Attribute") String attributes);

    @DELETE
    @Path("/{category}/{uuid}")
    public javax.ws.rs.core.Response deleteResource(@PathParam("category") String category, @PathParam("uuid") String uuid, @QueryParam("status") String status);
}
