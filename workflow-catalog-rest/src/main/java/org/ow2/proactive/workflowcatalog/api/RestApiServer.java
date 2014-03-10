package org.ow2.proactive.workflowcatalog.api;

import org.apache.log4j.Logger;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.FormatterHelper;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.ReferencesBean;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.WorkflowBean;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.WorkflowParametersBean;
import org.ow2.proactive.workflowcatalog.utils.scheduling.JobSubmissionException;

import java.util.Collection;

public class RestApiServer implements RestApi {

    private static Logger logger = Logger.getLogger(RestApiServer.class);

    @Override
    public Collection<WorkflowBean> getWorkflowList() {
        Core core = Core.getInstance();
        return FormatterHelper.formatToBean(core.getWorkflows());
    }

    @Override
    public ReferencesBean submitJob(WorkflowParametersBean parameters) {
        logger.debug(String.format("<<< %s", parameters.toString()));
        try {
            Core core = Core.getInstance();
            return new ReferencesBean(core.executeWorkflow(parameters.generateWorkflowParameters()));
        } catch (JobSubmissionException e) {
            logger.warn("Job submission error", e);
            return new ReferencesBean();
        }
    }

}

