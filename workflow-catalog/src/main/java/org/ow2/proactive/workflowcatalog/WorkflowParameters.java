package org.ow2.proactive.workflowcatalog;

//import org.apache.log4j.Logger;

import java.util.*;
import static java.util.Map.*;

public class WorkflowParameters {

    //private static final Logger logger = Logger.getLogger(WorkflowParameters.class.getName());

    private String name;
    private Map<String, String> variables;
    private Map<String, String> genericInformation;
    private Boolean strictMatch;

    public WorkflowParameters() {
        this.name = "UNDEFINED";
        this.variables = new HashMap<String, String>();
        this.genericInformation = new HashMap<String, String>();
        this.strictMatch = false;
    }

    public void setGenericInformation(Map<String, String> genericInformation) {
        this.genericInformation = genericInformation;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStrictMatch(boolean strictMatch) {
        this.strictMatch = strictMatch;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public Map<String, String> getGenericInformation() {
        return genericInformation;
    }

    public Boolean getStrictMatch() {
        return strictMatch;
    }

    public boolean matches(Workflow candidate) {
        if (strictMatch) {
            return matchesStrictly(candidate);
        } else {
            return matchesRelaxed(candidate);
        }
    }

    private boolean matchesRelaxed(Workflow candidate) {

        WorkflowParameters filter = this;

        if (!nameMatches(candidate, filter)) {
            //logger.debug(String.format( "candidate '%s' discarded : filter requires name '%s'", candidate.getName(), filter.name));
            return false;
        }

        return true;
    }

    private boolean matchesStrictly(Workflow candidate) {

        WorkflowParameters filter = this;

        if (!nameMatches(candidate, filter)) {
            //logger.debug(String.format( "candidate '%s' discarded : filter requires name '%s'", candidate.getName(), filter.name));
            return false;
        }

        for (Entry<String, String> candidateGenericInformationX: candidate.getGenericInformation().entrySet()) {
            if (notContained(candidateGenericInformationX, filter.genericInformation.entrySet())) {
                //logger.debug(String.format("candidate '%s' discarded : wrong generic information (there should be one '%s' in filter '%s')",
                //                                                 candidate.getName(), candidateGenericInformationX.toString(), filter.genericInformation.entrySet()));
                return false;
            }
        }

        for (String candidateVariableX: candidate.getVariables().keySet()) {
            if (notContained(candidateVariableX, filter.variables.keySet())) {
                //logger.debug(String.format("candidate '%s' discarded : wrong variable (there should be one '%s' in filter '%s')", candidate.getName(), candidateVariableX.toString(), filter.variables.keySet()));
                return false;
            }
        }

        return true;
    }

    private boolean nameMatches(Workflow candidate, WorkflowParameters filter) {
        return candidate.getName().matches(filter.name);
    }

    private static <T> boolean notContained(T entry, Set<T> set) {
        if (entry == null || set == null)
            return true;
        return !set.contains(entry);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(name);
        builder.append(",");
        builder.append(strictMatch);
        builder.append(",");
        builder.append(getVariables());
        builder.append(",");
        builder.append(getGenericInformation());
        builder.append("]");
        return builder.toString();
    }

}
