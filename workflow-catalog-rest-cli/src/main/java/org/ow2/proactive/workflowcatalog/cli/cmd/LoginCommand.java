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

import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;
import static org.ow2.proactive.workflowcatalog.cli.CLIException.REASON_OTHER;
import static org.ow2.proactive.workflowcatalog.cli.HttpResponseStatus.OK;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.ow2.proactive.workflowcatalog.cli.ApplicationContext;
import org.ow2.proactive.workflowcatalog.cli.CLIException;
import org.ow2.proactive.workflowcatalog.cli.utils.HttpResponseWrapper;
import org.ow2.proactive.workflowcatalog.cli.utils.StringUtility;

public class LoginCommand extends AbstractLoginCommand implements Command {

    public static final String USERNAME = "org.ow2.proactive.workflowcatalog.cli.cmd.LoginCommand.username";
    public static final String PASSWORD = "org.ow2.proactive.workflowcatalog.cli.cmd.LoginCommand.password";

    private String username;

    public LoginCommand(String username) {
        this.username = username;
    }

    @Override
    protected String login(ApplicationContext currentContext)
            throws CLIException {
        String password = currentContext.getProperty(PASSWORD, String.class);
        if (password == null) {
            password = new String(readPassword(currentContext, "password:"));
        }

        HttpPost request = new HttpPost(currentContext.getWcResourceUrl("login"));
        StringEntity entity = new StringEntity("username=" + username
                + "&password=" + password, APPLICATION_FORM_URLENCODED);
        request.setEntity(entity);
        HttpResponseWrapper response = execute(request, currentContext);
        if (statusCode(OK) == statusCode(response)) {
            return StringUtility.responseAsString(response).trim();
        } else {
            handleError("An error occurred while logging:", response,
                    currentContext);
            throw new CLIException(REASON_OTHER,
                    "An error occurred while logging.");
        }
    }

    @Override
    protected String getAlias(ApplicationContext currentContext) {
        return currentContext.getProperty(USERNAME, String.class);
    }

    @Override
    protected void setAlias(ApplicationContext currentContext) {
        currentContext.setProperty(USERNAME, username);
    }
}
