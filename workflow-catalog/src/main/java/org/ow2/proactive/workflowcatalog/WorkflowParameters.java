package org.ow2.proactive.workflowcatalog;

import org.apache.log4j.Logger;

import java.util.*;

public class WorkflowParameters {

    private static final Logger logger = Logger.getLogger(WorkflowParameters.class.getName());

    protected String workflowName;
    protected Map<String, String> variables;
    protected Map<String, String> genericInformation;

    public WorkflowParameters() {
        workflowName = "UNDEFINED";
        variables = new HashMap<String, String>();
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public boolean matches(Workflow candidate) {

        WorkflowParameters filter = this;

        if (!candidate.getName().matches(filter.workflowName)) {
            logger.debug(
                    String.format(
                            "%s discarded : wrong name (%s)",
                            candidate.getName(), filter.workflowName));
            return false;
        }

        for (Map.Entry<String, String> gi: candidate.getGenericInformation().entrySet()) {
            if (notContained(gi, filter.variables.entrySet())) {
                logger.debug(
                        String.format(
                                "%s discarded : wrong variable (should be %s)",
                                candidate.getName(), gi.toString()));
                return false;
            }
        }

        return true;
    }

    private static <T> boolean notContained(T entry, Set<T> set) {
        if (entry == null || set == null)
            return true;
        return !set.contains(entry);
    }

}
