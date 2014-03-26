package org.ow2.proactive.workflowcatalog.api.utils.formatter.beans;

import java.util.Collections;
import java.util.Map;

import org.ow2.proactive.workflowcatalog.WorkflowParameters;

public class WorkflowParametersBean {

    public String name;
    public Map<String, String> variables;
    public Map<String, String> genericInformation;
    public Boolean strictMatch;

    public WorkflowParametersBean() {
        name = new String();
        variables = Collections.EMPTY_MAP;
        genericInformation = Collections.EMPTY_MAP;
        strictMatch = false;
    }

    public WorkflowParametersBean(WorkflowParameters workflow) {
        name = workflow.getName();
        variables = workflow.getVariables();
        genericInformation = workflow.getGenericInformation();
        strictMatch = workflow.getStrictMatch();
    }

    public WorkflowParameters generateWorkflowParameters() {
        WorkflowParameters wp = new WorkflowParameters();
        wp.setName(name);
        wp.setVariables(variables);
        wp.setGenericInformation(genericInformation);
        wp.setStrictMatch(strictMatch);
        return wp;
    }
}
