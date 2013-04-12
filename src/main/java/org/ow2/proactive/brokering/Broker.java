package org.ow2.proactive.brokering;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public interface Broker {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/catalog")
    public Catalog getCatalog();

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/rules")
    public Rules getRules();

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/")
    public Response request(@HeaderParam("attributes") Attributes attributes);

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/")
    public Response request(@HeaderParam("action") String action, @HeaderParam("attributes") Attributes attributes);

}