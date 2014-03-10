package org.ow2.proactive.workflowcatalog.api;

import org.ow2.proactive.workflowcatalog.References;
import org.ow2.proactive.workflowcatalog.WorkflowParameters;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.ReferencesBean;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.WorkflowBean;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.WorkflowParametersBean;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/workflows")
public interface RestApi {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Collection<WorkflowBean> getWorkflowList();

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/job/")
    public ReferencesBean submitJob(WorkflowParametersBean parameters);

}
