package org.ow2.proactive.brokering.occi;

import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.occi.api.Occi;
import org.ow2.proactive.brokering.occi.infrastructure.Utils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

public class OcciServer implements Occi {
    private static Logger logger = Logger.getLogger(OcciServer.class);
    private static Database db = Database.getInstance();

    // ************* OCCI SERVER MANAGEMENT **************

    @Override
    public Response occiAction(String action) {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }

    @Override
    public Response getOcciState(String action) {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }

    // ************* RESOURCE MANAGEMENT **************

    @Override
    public Response createResource(String host, String category, String attributes) {
        attributes = attributes.replaceAll("\"", "");
        logger.info("------------------------------------------------------------------------");
        logger.info("Create : host = [" + host + "], category = [" + category + "]");
        logger.debug("         attributes = [" + attributes + "]");
        try {
            UUID uuid = UUID.randomUUID();
            attributes += ",action.state=pending,occi.core.id=" + uuid;
//            attributes += ",action.state=\"pending\", occi.core.id=\"" + uuid + "\"";
            Resource resource = Resource.factory(uuid, host, category, Utils.buildMap(attributes));
            db.store(resource);

            if (!resource.create()) {
                logger.debug("Response : CODE:" + Response.Status.BAD_REQUEST);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            Response.ResponseBuilder responseBuilder = Response.status(Response.Status.CREATED);
            responseBuilder.header("X-OCCI-Location", resource.getUrl());
            responseBuilder.entity("X-OCCI-Location: " + resource.getUrl() + "\n");
            logger.debug("Response : [X-OCCI-Location: " + resource.getUrl() + "] CODE:" + Response.Status.CREATED);
            return responseBuilder.build();

        } catch (Throwable e) {
            logger.error("Error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } finally {
            logger.info("------------------------------------------------------------------------");
        }
    }

    @Override
    public Response getAllResources(String category) {
        logger.info("------------------------------------------------------------------------");
        logger.info("Get list : category = [" + category + "]");
        try {
            String locations = "";
            for (Resource resource : Resource.getResources().values()) {
                if (resource.getCategory().equalsIgnoreCase(category)) {
                    locations += "X-OCCI-Location: " + resource.getUrl() + "\n";
                }
            }
            Response.ResponseBuilder response = Response.status(Response.Status.OK);
            response.type(MediaType.TEXT_PLAIN_TYPE);
            response.entity(locations);
            logger.debug("Response : *" + locations.length() + " locations* CODE:" + Response.Status.OK);
            return response.build();

        } catch (Throwable e) {
            logger.error("Error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } finally {
            logger.info("------------------------------------------------------------------------");
        }
    }

    @Override
    public Response getResource(String category, String uuid, String attribute) {
        logger.info("------------------------------------------------------------------------");
        logger.info("Get : category = [" + category + "], uuid = [" + uuid + "], attribute = [" + attribute +"]");
        try {
            Resource resource = Resource.getResources().get(UUID.fromString(uuid));
            if (resource == null || !resource.getCategory().equalsIgnoreCase(category)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            Response.ResponseBuilder response = Response.status(Response.Status.OK);
            response.type(MediaType.TEXT_PLAIN_TYPE);
            if(attribute == null) {
            response.entity(resource);
            } else {
                System.out.println("Returning an attribute value : " + attribute);
                logger.info("Returning an attribute value : " + attribute);
                response.entity(resource.getAttributes().get(attribute));
            }
            logger.debug("Response : *Returned resource : " + resource.getUuid() + "* CODE:" + Response.Status.OK);
            return response.build();

        } catch (Throwable e) {
            logger.error("Error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getClass().getName() + ": " + e.getMessage()).build();
        } finally {
            logger.info("------------------------------------------------------------------------");
        }
    }

    @Override
    public Response updateResource(String category, String uuid, String action, String attributes) {
        logger.info("------------------------------------------------------------------------");
        logger.info("Update : category = [" + category + "], uuid = [" + uuid + "], action = [" + action + "]");
        logger.info("         attributes = [" + attributes + "]");
        try {
            Resource resource = Resource.getResources().get(UUID.fromString(uuid));
            if (resource == null || !resource.getCategory().equalsIgnoreCase(category)) {
                logger.debug("Response : NOT_FOUND:" + Response.Status.NOT_FOUND);
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            Map<String, String> newAttributes = Utils.buildMap(attributes);
            for (String key : newAttributes.keySet()) {
                String attribute = resource.getAttributes().get(key);
                if (attribute == null) {
                    logger.debug("Unexpected attribute: " + key);
                    continue;
                }
                resource.getAttributes().put(key, newAttributes.get(key));
            }
            Database.getInstance().store(resource);

            if (action == null && attributes != null && !attributes.contains(".state")) {
                action = "hw_update";
            }

            if (action != null) {
                resource.getAttributes().put("action.state", "pending");
            }

            Response.ResponseBuilder response = null;
            boolean brokerResponse = false;
            if (action != null) {
                resource.update(action);
            }
            if (brokerResponse) {
                response = Response.status(Response.Status.ACCEPTED); // Async operation is submitted
            } else {
                response = Response.status(Response.Status.OK);
            }
            response.header("X-OCCI-Location", resource.getUrl());
            response.entity("X-OCCI-Location: " + resource.getUrl() + "\n");
            logger.debug("Response : [X-OCCI-Location: " + resource.getUrl() + "] CODE:" + Response.Status.OK);
            return response.build();

        } catch (Throwable e) {
            logger.error("Error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } finally {
            logger.info("------------------------------------------------------------------------");
        }
    }

    @Override
    public Response fullUpdateResource(String category, String uuid, String action, String attributes) {
        return updateResource(category, uuid, action, attributes);
    }

    @Override
    public Response deleteResource(String category, String uuid, String status) {
        logger.info("------------------------------------------------------------------------");
        try {
            if (status == null) {
                logger.info("Delete request : category = [" + category + "], uuid = [" + uuid + "]");
                return updateResource(category, uuid, "delete", "");
            }
            logger.info("Delete URL : category = [" + category + "], uuid = [" + uuid + "], status = [" + status + "]");
            if (status.equalsIgnoreCase("done")) {
                Resource.getResources().remove(UUID.fromString(uuid));
                db.delete(UUID.fromString(uuid));
                logger.info("------------------------------------------------------------------------");
                return Response.status(Response.Status.OK).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (Throwable e) {
            logger.error("Error", e);
            logger.info("------------------------------------------------------------------------");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
