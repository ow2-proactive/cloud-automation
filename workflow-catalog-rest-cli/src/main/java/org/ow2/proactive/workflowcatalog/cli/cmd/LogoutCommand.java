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

import org.apache.http.client.methods.HttpPost;
import org.ow2.proactive.workflowcatalog.cli.ApplicationContext;
import org.ow2.proactive.workflowcatalog.cli.CLIException;
import org.ow2.proactive.workflowcatalog.cli.utils.HttpResponseWrapper;

import static org.ow2.proactive.workflowcatalog.cli.CLIException.REASON_OTHER;
import static org.ow2.proactive.workflowcatalog.cli.HttpResponseStatus.NO_CONTENT;

public class LogoutCommand extends AbstractCommand implements Command {


    public LogoutCommand() {
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        HttpPost request = new HttpPost(currentContext.getWcResourceUrl("logout"));
        HttpResponseWrapper response = execute(request, currentContext);
        if (statusCode(NO_CONTENT) == statusCode(response)) {
            currentContext.setSessionId(null);
        } else {
            handleError("An error occurred while logging out:", response,
                    currentContext);
            throw new CLIException(REASON_OTHER, "An error occurred while logging out.");
        }
    }

}
