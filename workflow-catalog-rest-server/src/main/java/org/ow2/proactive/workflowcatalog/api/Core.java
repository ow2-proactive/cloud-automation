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

import java.io.IOException;
import java.util.Collection;

import javax.xml.transform.TransformerException;

import org.ow2.proactive.workflowcatalog.*;
import org.ow2.proactive.workflowcatalog.security.SchedulerRestSession;
import org.ow2.proactive.workflowcatalog.utils.scheduling.*;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.apache.shiro.session.UnknownSessionException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;

public enum Core {
    INSTANCE();

    private Core() {}

    public Collection<Workflow> getWorkflows()
            throws NotConnectedRestException, WorkflowsRetrievalException {
        ISchedulerProxy proxy = getScheduler();
        return new StudioApiCatalog(proxy).getWorkflows();
    }

    public References executeWorkflow(WorkflowParameters data)
            throws NotConnectedRestException, JobSubmissionException {
        References references = new References();
        Collection<Workflow> workflows;
        try {
            workflows = new StudioApiCatalog(getScheduler()).getWorkflows(data);
        } catch (WorkflowsRetrievalException e) {
            throw new JobSubmissionException("Cannot list workflows", e);
        }

        for (Workflow w: workflows) {
            try {
                JobIdData jsonResponse = getScheduler().submitJob(w.configure(data.getVariables()));
                references.add(Reference.buildJobReference(w.getName(), jsonResponse));
            } catch (JobCreationException e) {
                throw new JobSubmissionException("Error creating job", e);
            } catch (JobParsingException e) {
                throw new JobSubmissionException("Error parsing job", e);
            } catch (TransformerException e) {
                throw new JobSubmissionException("Unexpected error", e);
            } catch (IOException e) {
                throw new JobSubmissionException(e);
            }
        }

        return references;
    }

    private ISchedulerProxy getScheduler() throws NotConnectedRestException {
        try {
            return SchedulerRestSession.getScheduler();
        } catch (UnknownSessionException e) {
            throw new NotConnectedRestException(e);
        }
    }    

}

