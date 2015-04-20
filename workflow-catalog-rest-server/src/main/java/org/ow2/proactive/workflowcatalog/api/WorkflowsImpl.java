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

import java.util.Collection;

import org.apache.log4j.Logger;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.FormatterHelper;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.ReferencesBean;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.WorkflowBean;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.WorkflowParametersBean;
import org.ow2.proactive.workflowcatalog.utils.scheduling.JobSubmissionException;
import org.ow2.proactive.workflowcatalog.utils.scheduling.WorkflowsRetrievalException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;

public class WorkflowsImpl implements Workflows {


    private static Logger logger = Logger.getLogger(WorkflowsImpl.class);

    @Override
    public Collection<WorkflowBean> getWorkflowList()
            throws NotConnectedRestException, WorkflowsRetrievalException {
        Core core = Core.INSTANCE;
        return FormatterHelper.formatToBean(core.getWorkflows());
    }

    @Override
    public ReferencesBean submitJob(WorkflowParametersBean parameters)
            throws NotConnectedRestException, JobSubmissionException {
        logger.debug(String.format("<<< %s", parameters.toString()));
        Core core = Core.INSTANCE;
        return new ReferencesBean(core.executeWorkflow(parameters.generateWorkflowParameters()));
    }

}

