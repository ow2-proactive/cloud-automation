/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 2013-2015 ActiveEon
 * 
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * $$ACTIVEEON_INITIAL_DEV$$
 */


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
