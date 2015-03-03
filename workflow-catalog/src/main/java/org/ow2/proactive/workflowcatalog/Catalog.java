package org.ow2.proactive.workflowcatalog;

import org.ow2.proactive.workflowcatalog.utils.scheduling.WorkflowsRetrievalException;

import java.util.Collection;

public interface Catalog {
    public Collection<Workflow> getWorkflows(WorkflowParameters filter) throws WorkflowsRetrievalException;
    public Collection<Workflow> getWorkflows() throws WorkflowsRetrievalException;
    public void forceUpdate();
}

