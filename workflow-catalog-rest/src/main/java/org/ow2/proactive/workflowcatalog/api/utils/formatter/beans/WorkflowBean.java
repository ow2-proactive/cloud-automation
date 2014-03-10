package org.ow2.proactive.workflowcatalog.api.utils.formatter.beans;

import com.google.gson.Gson;
import org.ow2.proactive.workflowcatalog.Workflow;
import java.util.Collections;
import java.util.Map;

public class WorkflowBean {

    public String name;
    public Map<String, String> variables;
    public Map<String, String> genericInformation;

    public WorkflowBean(Workflow workflow) {
        name = workflow.getName();
        variables = workflow.getVariables();
        genericInformation = workflow.getGenericInformation();
    }

    public WorkflowBean() {
        name = new String();
        variables = Collections.EMPTY_MAP;
        genericInformation = Collections.EMPTY_MAP;
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}
