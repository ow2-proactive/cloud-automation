package org.ow2.proactive.workflowcatalog.api.utils.formatter.beans;

import org.ow2.proactive.workflowcatalog.Workflow;

import java.util.Collection;
import java.util.Map;

public class WorkflowBeanOutput {

    public String name;
    public Collection<String> variables;
    public Map<String, String> genericInformation;

    public WorkflowBeanOutput(Workflow workflow) {
        name = workflow.getName();
        variables = workflow.getVariables().keySet();
        genericInformation = workflow.getGenericInformation();
    }
}
