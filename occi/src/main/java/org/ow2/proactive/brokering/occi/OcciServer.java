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


package org.ow2.proactive.brokering.occi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.ow2.proactive.brokering.Broker;
import org.ow2.proactive.brokering.occi.api.Occi;
import org.ow2.proactive.brokering.occi.categories.Categories;
import org.ow2.proactive.brokering.occi.categories.Utils;
import org.ow2.proactive.brokering.occi.database.Database;
import org.ow2.proactive.brokering.occi.database.DatabaseFactory;
import org.ow2.proactive.brokering.updater.Updater;
import org.ow2.proactive.workflowcatalog.Reference;
import org.ow2.proactive.workflowcatalog.References;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.log4j.Logger;


public class OcciServer implements Occi {

    private static Logger logger = Logger.getLogger(OcciServer.class);

    private Broker broker;
    private Updater updater;
    private DatabaseFactory databaseFactory;

    private String serverPrefixUrl;

    @Inject
    public OcciServer(Broker broker, Updater updater, DatabaseFactory databaseFactory,
      @Named("server.prefix") String serverPrefixUrl) {
        this.broker = broker;
        this.updater = updater;
        this.serverPrefixUrl = serverPrefixUrl;
        this.databaseFactory = databaseFactory;
    }

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
            Resource resource = ResourceBuilder.factory(uuid, category, Utils.buildMap(attributes));
            storeInDB(resource);

            References references = broker.request(category, Resource.OP_CREATE, resource.getAttributes());
            addResourceToUpdateQueue(resource, references);

            if (!references.areAllSubmitted()) {
                logger.debug("Response : CODE:" + Response.Status.BAD_REQUEST);
                return Response.status(Response.Status.BAD_REQUEST).entity(references.getSummary()).build();
            }

            Response.ResponseBuilder responseBuilder = Response.status(Response.Status.CREATED);
            String resourceUri = resource.getFullPath(serverPrefixUrl);
            responseBuilder.header("X-OCCI-Location", resourceUri);
            responseBuilder.entity(new ResourceLocation(resourceUri));
            logger.debug("Response : [X-OCCI-Location: " + resourceUri + "] CODE:" + Response.Status.CREATED);
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
            for (Resource resource : findAllInDB()) {
                if (resource.getCategory().equalsIgnoreCase(category)) {
                    resource = fillLinks(resource);
                    resource = fillActions(resource);
                    filteredResources.add(resource);
                }
            }
            Resources resources = new Resources(filteredResources, serverPrefixUrl);
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
        logger.info(
          "Get : category = [" + category + "], uuid = [" + uuid + "], attribute = [" + attribute + "]");
        try {
            Resource resource = findInDB(uuid);
            if (resource == null || !resource.getCategory().equalsIgnoreCase(category)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            resource = fillLinks(resource);
            resource = fillActions(resource);
            Response.ResponseBuilder response = Response.status(Response.Status.OK);
            if (attribute == null) {
                response.entity(resource);
            } else {
                logger.info("Returning an attribute value : " + attribute);
                response.entity(resource.getAttributes().get(attribute));
            }
            logger.debug(
              "Response : *Returned resource : " + resource.getUuid() + "* CODE:" + Response.Status.OK);
            return response.build();

        } catch (Throwable e) {
            logger.error("Error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
              .entity(e.getClass().getName() + ": " + e.getMessage()).build();
        } finally {
            logger.info("------------------------------------------------------------------------");
        }
    }

    private Resource fillLinks(Resource resource) {
        if (resource.getAttributes().get("links") != null) {
            String[] linksUuid = resource.getAttributes().get("links").split(",");
            List<Resource> links = new ArrayList<Resource>();
            for (String linkUuid : linksUuid) {
                Resource linkedResource = findInDB(linkUuid);
                if (linkedResource != null) {
                    linkedResource = fillActions(linkedResource);
                    links.add(linkedResource);
                }
            }
            resource.setLinks(links);
        }
        return resource;
    }

    private Resource fillActions(Resource resource) {
        resource.setActions(broker.listPossibleActions(resource.getCategory(), resource.getAttributes()));
        return resource;
    }

    @Override
    public Response updateResource(String category, String uuid, String action, String attributes) {
        logger.info("------------------------------------------------------------------------");
        logger.info("Update : category = [" + category + "], uuid = [" + uuid + "], action = [" + action + "]");
        logger.info("         attributes = [" + attributes + "]");
        try {
            Resource resource = findInDB(uuid);
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

            storeInDB(resource);

            addActionAttributes(action, resource, newAttributes);

            References references = new References();
            Response.ResponseBuilder response;
            if (action != null) {
                references = broker.request(category, Resource.OP_UPDATE, action, resource.getAttributes());
                addResourceToUpdateQueue(resource, references);

            }
            response = Response.status(Response.Status.OK);
            response.entity(references.getSummary());
            String resourceUri = resource.getFullPath(serverPrefixUrl);
            response.header("X-OCCI-Location", resourceUri);
            response.entity(new ResourceLocation(resourceUri));
            logger.debug("Response : [X-OCCI-Location: " + resourceUri + "] CODE:" + Response.Status.OK);
            return response.build();

        } catch (Throwable e) {
            logger.error("Error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } finally {
            logger.info("------------------------------------------------------------------------");
        }
    }

    private void addActionAttributes(String action, Resource resource, Map<String, String> newAttributes) {
        if (action != null) {
            resource.getAttributes().put("action.state", "pending");
            for (String key : newAttributes.keySet()) {
                resource.getAttributes().put(key, newAttributes.get(key));
            }
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
            logger.info(
              "Delete URL : category = [" + category + "], uuid = [" + uuid + "], status = [" + status + "]");
            if (status.equalsIgnoreCase("done")) {
                deleteFromDB(uuid);
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

    private void addResourceToUpdateQueue(Resource resource,
      References references) {
        for (Reference ref : references) {
            if (Reference.Nature.NATURE_JOB.equals(ref.getNatureOfReference())) {
                updater.addResourceToTheUpdateQueue(ref, getResource(resource.getAttributes()));
            }
        }
    }

    private Resource getResource(Map<String, String> attributes) {
        return findInDB(attributes.get("occi.core.id"));
    }

    private void storeInDB(Resource resource) {
        Database db = databaseFactory.build();
        db.store(resource);
        db.close();
    }

    private Resource findInDB(String uuid) {
        Database db = databaseFactory.build();
        Resource resource = db.load(uuid);
        db.close();
        return resource;
    }

    private List<Resource> findAllInDB() {
        Database db = databaseFactory.build();
        List<Resource> resources = db.getAllResources();
        db.close();
        return resources;
    }

    private void deleteFromDB(String uuid) {
        Database db = databaseFactory.build();
        db.delete(uuid);
        db.close();
    }

    /** For testing */
    public void setUpdater(Updater updater) {
        this.updater = updater;
    }

    public void linkResources(String sourceLocation, String targetLocation) {
        String sourceUuid = toRelativePath(sourceLocation);
        String targetUuid = toRelativePath(targetLocation);
        Resource source = findInDB(sourceUuid);
        Resource target = findInDB(targetUuid);
        addLinkToResource(source, target);
        addLinkToResource(target, source);

        storeInDB(source);
        storeInDB(target);
    }

    private void addLinkToResource(Resource source, Resource target) {
        // links is comma separated resource uuids
        if (source.getAttributes().get("links") == null) {
            source.getAttributes().put("links", target.getUuid());
        } else {
            source.getAttributes().put("links", source.getAttributes().get("links") + "," + target.getUuid());
        }
    }

    private String toRelativePath(String sourceLocation) {
        String[] urlParts = sourceLocation.split("/");
        return urlParts[urlParts.length - 1];
    }
}
