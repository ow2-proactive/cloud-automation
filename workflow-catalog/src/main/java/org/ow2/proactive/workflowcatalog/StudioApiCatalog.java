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

import org.apache.log4j.Logger;
import org.ow2.proactive.workflowcatalog.utils.scheduling.ISchedulerProxy;
import org.ow2.proactive.workflowcatalog.utils.scheduling.WorkflowsRetrievalException;
import java.util.*;

public class StudioApiCatalog implements Catalog {
    private static final Logger logger = Logger.getLogger(StudioApiCatalog.class.getName());
    private ISchedulerProxy proxy;

    public StudioApiCatalog(ISchedulerProxy proxy) {
        this.proxy = proxy;
    }

    public Collection<Workflow> getWorkflows(WorkflowParameters filter)
            throws WorkflowsRetrievalException {
        ArrayList<Workflow> result = new ArrayList<Workflow>();

        for (org.ow2.proactive_grid_cloud_portal.studio.Workflow w : proxy.listWorkflows()) {
            Workflow workflow = convert(w);
            workflow.update();
            if (filter == null || filter.matches(workflow))
                result.add(workflow);
        }

        return result;
    }

    public Collection<Workflow> getWorkflows() throws WorkflowsRetrievalException {
        return getWorkflows(null);
    }

    public void forceUpdate() {}

    private Workflow convert(org.ow2.proactive_grid_cloud_portal.studio.Workflow w) {
        return new Workflow(w.getName(), w.getXml());
    }
}

