package org.ow2.proactive.workflowcatalog.api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/workflows")
public interface RestApi {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/")
    public Response getWorkflowList();

}
