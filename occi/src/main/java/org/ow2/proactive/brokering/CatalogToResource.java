/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.brokering;

import java.util.HashMap;
import java.util.Map;

import org.ow2.proactive.brokering.occi.Resource;
import org.ow2.proactive.brokering.occi.ResourcesHandler;
import org.ow2.proactive.workflowcatalog.CatalogListener;
import org.ow2.proactive.workflowcatalog.Workflow;


class CatalogToResource implements CatalogListener {
    @Override
    public void added(Workflow addedWorkflow) {
        if (isAWorkflowToCreate(addedWorkflow)) {
            Map<String, String> attributes = new HashMap<String, String>();
            attributes.putAll(addedWorkflow.getVariables());
            attributes.putAll(addedWorkflow.getGenericInformation());
            // FIXME it is not clear which attributes/generic information from the workflow should be exposed
            attributes.remove("action");
            String resourceId = resourceIdFromWorkflow(addedWorkflow);
            attributes.put("occi.core.id", resourceId);
            ResourcesHandler.factory(resourceId, "template", attributes);
        }
    }

    @Override
    public void updated(Workflow updatedWorkflow) {
        if (isAWorkflowToCreate(updatedWorkflow)) {
            Resource resource = ResourcesHandler.getResources().get(resourceIdFromWorkflow(updatedWorkflow));
            resource.getAttributes().putAll(updatedWorkflow.getVariables());
            resource.getAttributes().putAll(updatedWorkflow.getGenericInformation());
            resource.getAttributes().remove("action");
        }
    }

    @Override
    public void removed(Workflow removedWorkflow) {
        if (isAWorkflowToCreate(removedWorkflow)) {
            ResourcesHandler.getResources().remove(resourceIdFromWorkflow(removedWorkflow));
        }
    }

    private boolean isAWorkflowToCreate(Workflow workflow) {
        return "create".equals(workflow.getGenericInformation("operation"));
    }

    private String resourceIdFromWorkflow(Workflow addedWorkflow) {
        return addedWorkflow.getName().replace(".xml", "");
    }
}
