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
