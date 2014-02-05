package org.ow2.proactive.workflowcatalog.api;

import org.apache.log4j.Logger;
import org.ow2.proactive.workflowcatalog.References;
import org.ow2.proactive.workflowcatalog.WorkflowParameters;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.JsonFormatterHelper;
import org.ow2.proactive.workflowcatalog.utils.scheduling.JobSubmissionException;

import javax.ws.rs.core.Response;

public class RestApiServer implements RestApi {

    private static Logger logger = Logger.getLogger(RestApiServer.class);

    @Override
    public Response getWorkflowList() {
        Core core = Core.getInstance();
        String entity = JsonFormatterHelper.format(core.getWorkflows());
        return buildResponse(Response.Status.OK, entity);
    }

    @Override
    public Response submitJob(String json) {
        logger.debug(String.format("<<< %s", json));
        WorkflowParameters parameters = JsonFormatterHelper.toWorkflowParameters(json);
        logger.debug(String.format("Parsed: %s", JsonFormatterHelper.format(parameters)));
        try {
            Core core = Core.getInstance();
            References references = core.executeWorkflow(parameters);
            return buildResponse(Response.Status.OK, JsonFormatterHelper.format(references));
        } catch (JobSubmissionException e) {
            return buildResponse(Response.Status.INTERNAL_SERVER_ERROR, JsonFormatterHelper.format(e));
        }
    }

    private Response buildResponse(Response.Status status, String entity) {
        Response.ResponseBuilder responseBuilder = Response.status(status);
        responseBuilder.entity(entity);
        logger.debug(String.format(">>> %s : %s", status, entity));
        return responseBuilder.build();
    }

}

