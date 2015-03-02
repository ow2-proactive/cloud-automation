package org.ow2.proactive.workflowcatalog;

import java.util.Collection;

public interface Catalog {
    public Collection<Workflow> getWorkflows(WorkflowParameters filter);
    public Collection<Workflow> getWorkflows();
    public void forceUpdate();
}

