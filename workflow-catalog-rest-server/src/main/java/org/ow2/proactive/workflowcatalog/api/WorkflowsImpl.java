package org.ow2.proactive.workflowcatalog.api;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.FormatterHelper;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.ReferencesBean;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.WorkflowBean;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.WorkflowParametersBean;
import org.ow2.proactive.workflowcatalog.utils.scheduling.JobSubmissionException;

public class WorkflowsImpl implements Workflows {


    private static Logger logger = Logger.getLogger(WorkflowsImpl.class);

    @Override
    public Collection<WorkflowBean> getWorkflowList() {
        Core core = Core.INSTANCE;
        return FormatterHelper.formatToBean(core.getWorkflows());
    }

    @Override
    public ReferencesBean submitJob(WorkflowParametersBean parameters) throws JobSubmissionException {
        logger.debug(String.format("<<< %s", parameters.toString()));
        Core core = Core.INSTANCE;
        return new ReferencesBean(core.executeWorkflow(parameters.generateWorkflowParameters()));
    }

}

