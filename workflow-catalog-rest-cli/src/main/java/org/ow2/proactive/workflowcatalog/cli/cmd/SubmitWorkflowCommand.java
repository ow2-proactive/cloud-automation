/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
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
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive.workflowcatalog.cli.cmd;

import org.ow2.proactive.workflowcatalog.WorkflowParameters;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.ReferencesBean;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.WorkflowParametersBean;
import org.ow2.proactive.workflowcatalog.cli.ApplicationContext;
import org.ow2.proactive.workflowcatalog.cli.CLIException;
import org.ow2.proactive.workflowcatalog.cli.rest.WorkflowCatalogClient;
import org.ow2.proactive.workflowcatalog.utils.scheduling.JobSubmissionException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;

import java.util.Map;


public class SubmitWorkflowCommand extends AbstractCommand implements Command {

    private String workflowName;
    private Map<String, String> variables;
    private Map<String, String> genericInformation;

    public SubmitWorkflowCommand(String workflowName, Map<String, String> variables, Map<String, String> genericInformation) {
        this.workflowName = workflowName;
        this.variables = variables;
        this.genericInformation = genericInformation;
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        WorkflowCatalogClient client = currentContext.getWorkflowCatalogClient();

        WorkflowParameters params = new WorkflowParameters();
        params.setName(workflowName);

        if (variables != null)
            params.getVariables().putAll(variables);

        if (genericInformation != null)
            params.getGenericInformation().putAll(genericInformation);

        try {
            ReferencesBean references = client.getWorkflowsProxy().submitJob(new WorkflowParametersBean(params));
            writeLine(currentContext, "%s", references.generateReferences().getSummary());
        } catch (NotConnectedRestException e) {
            handleError("Not connected", e, currentContext);
        } catch (JobSubmissionException e) {
            handleError("An error occurred during job submission", e, currentContext);
        }
    }
}
