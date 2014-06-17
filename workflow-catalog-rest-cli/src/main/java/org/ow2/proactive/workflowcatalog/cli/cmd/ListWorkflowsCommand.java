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

import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.WorkflowBean;
import org.ow2.proactive.workflowcatalog.cli.ApplicationContext;
import org.ow2.proactive.workflowcatalog.cli.CLIException;
import org.ow2.proactive.workflowcatalog.cli.console.JLineDevice;
import org.ow2.proactive.workflowcatalog.cli.rest.WorkflowCatalogClient;
import org.ow2.proactive.workflowcatalog.cli.utils.StringUtility;

import java.util.Collection;
import java.util.Map;

public class ListWorkflowsCommand extends AbstractCommand implements Command {

    public ListWorkflowsCommand() {}

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        WorkflowCatalogClient client = currentContext.getWorkflowCatalogClient();

        Collection<WorkflowBean> workflows = client.getWorkflowsProxy().getWorkflowList();
        for (WorkflowBean workflow: workflows) {
            writeLine(currentContext, "%s", StringUtility.string(workflow));
            addAutocomplete(currentContext, workflow);

        }
    }

    private void addAutocomplete(ApplicationContext currentContext, WorkflowBean workflow) {
        JLineDevice jline = ((JLineDevice)currentContext.getDevice());
        String submitCmd = generateSubmitCommand(workflow);
        jline.addAutocompleteCommand(submitCmd);
    }

    private String generateSubmitCommand(WorkflowBean workflow) {
        StringBuilder cmd = new StringBuilder();
        cmd.append("submitworkflow('");
        cmd.append(workflow.name);
        cmd.append("',");
        cmd.append(createGroovyMapCmd(workflow.variables));
        cmd.append(",");
        cmd.append(createGroovyMapCmd(workflow.genericInformation));
        cmd.append(")");
        return cmd.toString();
    }

    private String createGroovyMapCmd(Map<String, String> map) {
        StringBuilder cmd = new StringBuilder();

        cmd.append("[");
        if (map.size() != 0)
            for (Map.Entry var: map.entrySet()) {
                cmd.append("'");
                cmd.append(var.getKey());
                cmd.append("':'");
                cmd.append(var.getValue());
                cmd.append("',");
            }
        else
            cmd.append(":");

        cmd.append("]");
        return cmd.toString();
    }

}
