package org.ow2.proactive.workflowcatalog.api;

import org.apache.log4j.Logger;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.JsonFormatterHelper;

import javax.ws.rs.core.Response;

public class RestApiServer implements RestApi {

    private static Logger logger = Logger.getLogger(RestApiServer.class);

    @Override
    public Response getWorkflowList() {
        Core core = Core.getInstance();
        String entity = JsonFormatterHelper.format(core.getWorkflows());
        return buildResponse(Response.Status.OK, entity);
    }

    private Response buildResponse(Response.Status status, String entity) {
        Response.ResponseBuilder responseBuilder = Response.status(status);
        responseBuilder.entity(entity);
        logger.debug(">>> " + entity);
        return responseBuilder.build();
    }
}
