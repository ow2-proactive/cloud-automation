package org.ow2.proactive.brokering.occi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.ow2.proactive.brokering.Broker;
import org.ow2.proactive.brokering.Configuration;
import org.ow2.proactive.brokering.occi.api.Occi;
import org.ow2.proactive.brokering.occi.categories.Categories;
import org.ow2.proactive.brokering.occi.categories.Utils;
import org.ow2.proactive.workflowcatalog.References;
import org.apache.log4j.Logger;

public class OcciServer implements Occi {

    private static Logger logger = Logger.getLogger(OcciServer.class);

    private static Database db;
    private static String prefixUrl;

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
            String uuid = UUID.randomUUID().toString();
            attributes += ",action.state=pending,occi.core.id=" + uuid;
            //            attributes += ",action.state=\"pending\", occi.core.id=\"" + uuid + "\"";
            Resource resource = Resource.factory(uuid, category, Utils.buildMap(attributes));
            db.store(resource);

            References references = resource.create();
            if (!references.areAllSubmitted()) {
                logger.debug("Response : CODE:" + Response.Status.BAD_REQUEST);
                return Response.status(Response.Status.BAD_REQUEST).entity(references.getSummary()).build();
            }

            Response.ResponseBuilder responseBuilder = Response.status(Response.Status.CREATED);
            responseBuilder.header("X-OCCI-Location", resource.getUrl(prefixUrl));
            responseBuilder.entity("X-OCCI-Location: " + resource.getUrl(prefixUrl) + "\n");
            logger.debug("Response : [X-OCCI-Location: " + resource.getUrl(prefixUrl) + "] CODE:" + Response.Status.CREATED);
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
            List<Resource> filteredResources = new ArrayList<Resource>();
            for (Resource resource : Resource.getResources().values()) {
                if (resource.getCategory().equalsIgnoreCase(category)) {
                    filteredResources.add(resource);
                }
            }
            Resources resources = new Resources(filteredResources, prefixUrl);
            Response.ResponseBuilder response = Response.status(Response.Status.OK);
            response.entity(resources);
            logger.debug("Response : *" + resources.size() + " locations* CODE:" + Response.Status.OK);
            return response.build();

        } catch (Throwable e) {
            logger.error("Error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } finally {
            logger.info("------------------------------------------------------------------------");
        }
    }

    @Override
    public Response RFC5785() {
        return Response.status(Response.Status.OK).build();
    }

    @Override
    public Response discovery() {
        return Response.status(Response.Status.OK).entity(Categories.list()).build();
    }

    @Override
    public Response getResource(String category, String uuid, String attribute) {
        logger.info("------------------------------------------------------------------------");
        logger.info("Get : category = [" + category + "], uuid = [" + uuid + "], attribute = [" + attribute + "]");
        try {
            Resource resource = Resource.getResources().get(uuid);
            if (resource == null || !resource.getCategory().equalsIgnoreCase(category)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            Response.ResponseBuilder response = Response.status(Response.Status.OK);
            if (attribute == null) {
                response.entity(resource);
            } else {
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
            Resource resource = Resource.getResources().get(uuid);
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
            db.store(resource);

            if (action != null) {
                resource.getAttributes().put("action.state", "pending");
            }

            References references = new References();
            Response.ResponseBuilder response = null;
            boolean brokerResponse = false;
            if (action != null) {
                references = resource.update(action);
            }
            if (brokerResponse) {
                response = Response.status(Response.Status.ACCEPTED); // Async operation is submitted
            } else {
                response = Response.status(Response.Status.OK);
            }
            response.entity(references.getSummary());
            response.header("X-OCCI-Location", resource.getUrl(prefixUrl));
            response.entity("X-OCCI-Location: " + resource.getUrl(prefixUrl) + "\n");
            logger.debug("Response : [X-OCCI-Location: " + resource.getUrl(prefixUrl) + "] CODE:" + Response.Status.OK);
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
                Resource.getResources().remove(uuid);
                db.delete(uuid);
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


    public OcciServer() {
        if (db == null) {
            db = Database.getInstance();

            try {
                Configuration config = Utils.getConfiguration();
                prefixUrl = config.server.prefix;
            } catch (JAXBException e) {
                logger.warn(e);
            }
        }
        // force creation when REST API is started
        Broker.getInstance();
    }

    public static void setDatabase(Database db) {
        OcciServer.db = db;
    }
}
