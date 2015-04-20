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


package org.ow2.proactive.brokering.updater;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import javax.ws.rs.core.Response;

import org.ow2.proactive.brokering.occi.Resource;
import org.ow2.proactive.brokering.occi.api.Occi;
import org.ow2.proactive.brokering.occi.categories.Utils;
import org.ow2.proactive.brokering.updater.requests.CreateInstanceRequest;
import org.ow2.proactive.brokering.updater.requests.UnknownRequest;
import org.ow2.proactive.brokering.updater.requests.UpdateAttributeRequest;
import org.ow2.proactive.brokering.updater.requests.UpdateInstanceRequest;
import org.ow2.proactive.workflowcatalog.Reference;
import org.ow2.proactive.workflowcatalog.utils.HttpUtility;
import org.ow2.proactive.workflowcatalog.utils.scheduling.ISchedulerProxy;
import org.ow2.proactive.workflowcatalog.utils.scheduling.JobNotFinishedException;
import org.ow2.proactive.workflowcatalog.utils.scheduling.TasksResults;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.log4j.Logger;

public class Updater {

    private static final Logger logger = Logger.getLogger(Updater.class.getName());

    private Occi occi;
    private ISchedulerProxy scheduler;
    private LinkedBlockingQueue<UpdateUnit> queue;

    private String prefixUrl;

    @Inject
    public Updater(Occi occi, ISchedulerProxy scheduler,
      @Named("updater.refresh") Long periodMs,
      @Named("server.prefix") String prefixUrl) {
        this.occi = occi;
        this.scheduler = scheduler;
        this.prefixUrl = prefixUrl;

        this.queue = new LinkedBlockingQueue<UpdateUnit>();
        Timer timer = new Timer("Updater");
        timer.schedule(new UpdaterTask(), 0, periodMs);
    }

    public synchronized int getUpdateQueueSize() {
        return queue.size();
    }

    public synchronized void addResourceToTheUpdateQueue(Reference jobReference, Resource resource) {
        try {
            queue.put(new UpdateUnit(resource, jobReference));
        } catch (InterruptedException e) {
            logger.warn("Not expected", e);
        }
    }

    // INNER CLASSES

    class UpdateUnit {

        Reference reference;
        Resource resource;

        UpdateUnit(Resource resource, Reference reference) {
            this.resource = resource;
            this.reference = reference;
        }

        public String toString() {
            return resource.getUuid() + " : job " + reference.getId();
        }

    }

    class UpdaterTask extends TimerTask {

        @Override
        public void run() {

            Iterator<UpdateUnit> it = queue.iterator();

            while (it.hasNext()) {
                UpdateUnit u = it.next();
                handleJobResult(u);
            }

        }

        private void handleJobResult(UpdateUnit u) {

            String currentInstanceUrl = u.resource.getFullPath(prefixUrl);

            try {

                TasksResults tasksResults = scheduler.getAllTaskResults(u.reference.getId());
                OcciTasksResults occiTaskResults = new OcciTasksResults(tasksResults);


                // Create new category instances if required
                List<CreateInstanceRequest> createRequests = occiTaskResults.getCreateInstanceRequests();
                for (CreateInstanceRequest request: createRequests) {
                    request.processSpecialAttributes(currentInstanceUrl);
                    try {
                        String newInstanceUrl = createAnotherCategoryInstance(request);
                        linkCurrentInstanceAndNewInstance(currentInstanceUrl, newInstanceUrl);
                        occiTaskResults.add(
                                new UpdateAttributeRequest(
                                        request.getAttributeToUpdateWithLocation(), newInstanceUrl));
                    } catch (Exception e) {
                        occiTaskResults.add(
                                new UnknownRequest(e.getMessage()));
                    }
                }

                // Update categories' instances if required
                List<UpdateInstanceRequest> updateRequests = occiTaskResults.getUpdateInstanceRequests();
                for (UpdateInstanceRequest request: updateRequests) {
                    request.processSpecialAttributes(currentInstanceUrl);
                    try {
                        updateAnotherCategoryInstance(request);
                    } catch (Exception e) {
                        occiTaskResults.add(
                                new UnknownRequest(e.getMessage()));
                    }
                }

                // Get the attributes to update the current category instance
                Map<String, String> attributesToUpdate = occiTaskResults.getUpdateAttributes();

                // Update the attributes of the current category instance
                Response r = occi.updateResource(
                        u.resource.getCategory(), u.resource.getUuid(),
                        null, Utils.buildString(attributesToUpdate));
                printLogsIfIncorrectExecution(r);

                removeUpdateItem(u);

            } catch (JobNotFinishedException e) {
                logger.debug("Waiting for: " + u);
            } catch (Exception e) {
                logger.warn("Unexpected error while updating: " + u, e);
                removeUpdateItem(u);
            }

        }

        private String createAnotherCategoryInstance(CreateInstanceRequest request) {
            logger.debug("Create requested: " + request);
            Response response = occi.createResource(null, request.getCategory(),
                                                    Utils.buildString(request.getAttributes()));

            Utils.checkResponse(response);
            return Utils.toUrl(response);
        }

        private void updateAnotherCategoryInstance(UpdateInstanceRequest request) {
            logger.debug("Update requested: " + request);
            Response response = occi.updateResource(request.getCategory(),
                                                    request.getUuid(), request.getAction(),
                                                    Utils.buildString(request.getAttributes()));
            Utils.checkResponse(response);
        }

        private void printLogsIfIncorrectExecution(Response r) {
            if (HttpUtility.isNotSuccessStatusCode(r.getStatus()))
                logger.warn("Error trying to update category instance: " + r);
        }

        private void removeUpdateItem(UpdateUnit u) {
            logger.debug("Removing '" + u + "' from update queue.");
            queue.remove(u);
        }

    }

    private void linkCurrentInstanceAndNewInstance(String sourceLocation, String targetLocation) {
        occi.linkResources(sourceLocation, targetLocation.replace("\n", ""));

    }

}
