package org.ow2.proactive.workflowcatalog.api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/workflows")
public interface RestApi {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response getWorkflowList();

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/job/")
    public Response submitJob(String json);

}
