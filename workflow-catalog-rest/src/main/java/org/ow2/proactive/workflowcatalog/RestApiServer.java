package org.ow2.proactive.brokering.wc;

import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.wc.api.RestApi;

import javax.ws.rs.core.Response;

public class RestApiServer implements RestApi {

    private static Logger logger = Logger.getLogger(RestApiServer.class);

    @Override
    public Response getWorkflowList() {
        String entity = "{}";
        Response.ResponseBuilder responseBuilder = Response.status(Response.Status.CREATED);
        responseBuilder.entity(entity);
        logger.debug("Response : [" + entity + "] CODE:" + Response.Status.CREATED);
        return responseBuilder.build();
    }
}
