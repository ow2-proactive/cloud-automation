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


package org.ow2.proactive.workflowcatalog.api;

import javax.security.auth.login.LoginException;

import org.ow2.proactive.workflowcatalog.SchedulerAuthentication;
import org.ow2.proactive.workflowcatalog.api.utils.ConfigurationHelper;
import org.ow2.proactive.workflowcatalog.utils.scheduling.ISchedulerProxy;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerLoginData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;

public class WorkflowCatalogSchedulerAuthentication extends SchedulerAuthentication {

    /** For testing */
    static SchedulerProxyFactory schedulerProxyFactory = new SchedulerProxyFactory();

    @Override
    protected ISchedulerProxy loginToSchedulerRestApi(SchedulerLoginData login) throws LoginException, SchedulerRestException {

        SchedulerLoginData loginData = ConfigurationHelper.getSchedulerLoginData(
          ConfigurationHelper.getConfiguration());

        loginData.schedulerUsername = login.schedulerUsername;
        loginData.schedulerPassword = login.schedulerPassword;
        loginData.schedulerCredentials = login.schedulerCredentials;

        return schedulerProxyFactory.create(loginData);
    }

}
