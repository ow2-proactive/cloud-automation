/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 2013-2015 ActiveEon
 * 
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * $$ACTIVEEON_INITIAL_DEV$$
 */


package org.ow2.proactive.brokering.occi.api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/occi")
@Produces({ MediaType.TEXT_PLAIN, "text/occi", MediaType.APPLICATION_JSON })
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
    public Response createResource(@HeaderParam("Host") String host, @PathParam("category") String category, @HeaderParam("X-OCCI-Attribute") String attributes);

    @GET
    @Path("/{category}")
    public Response getAllResources(@PathParam("category") String category);

    @GET
    @Path("/.well-known/org/ogf/occi/-/")
    public Response RFC5785();

    @GET
    @Path("/-/")
    public Response discovery();

    @GET
    @Path("/{category}/{uuid}")
    public Response getResource(@PathParam("category") String category, @PathParam("uuid") String uuid, @QueryParam("attribute") String attribute);

    @POST
    @Path("/{category}/{uuid}")
    public Response updateResource(@PathParam("category") String category, @PathParam("uuid") String uuid, @QueryParam("action") String action, @HeaderParam("X-OCCI-Attribute") String attributes);

    @PUT
    @Path("/{category}/{uuid}")
    public Response fullUpdateResource(@PathParam("category") String category, @PathParam("uuid") String uuid, @QueryParam("action") String action, @HeaderParam("X-OCCI-Attribute") String attributes);

    @DELETE
    @Path("/{category}/{uuid}")
    public Response deleteResource(@PathParam("category") String category, @PathParam("uuid") String uuid, @QueryParam("status") String status);

    void linkResources(String sourceLocation, String targetLocation);
}
