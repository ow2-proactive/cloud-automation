package org.ow2.proactive.brokering.updater;

import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.occi.Resource;
import org.ow2.proactive.brokering.occi.ResourcesHandler;
import org.ow2.proactive.brokering.occi.api.Occi;
import org.ow2.proactive.brokering.occi.categories.Utils;
import org.ow2.proactive.brokering.updater.requests.CreateInstanceRequest;
import org.ow2.proactive.brokering.updater.requests.FailedRequest;
import org.ow2.proactive.brokering.updater.requests.UpdateAttributeRequest;
import org.ow2.proactive.brokering.updater.requests.UpdateInstanceRequest;
import org.ow2.proactive.workflowcatalog.Reference;
import org.ow2.proactive.workflowcatalog.utils.HttpUtility;
import org.ow2.proactive.workflowcatalog.utils.scheduling.JobNotFinishedException;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerProxy;
import org.ow2.proactive.workflowcatalog.utils.scheduling.TasksResults;

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Updater {

    private static final Logger logger = Logger.getLogger(Updater.class.getName());

    private Occi occi;
    private SchedulerProxy scheduler;
    private LinkedBlockingQueue<UpdateUnit> queue;

    public Updater(Occi occi, SchedulerProxy scheduler, Long periodMs) {
        this.occi = occi;
        this.scheduler = scheduler;
        this.queue = new LinkedBlockingQueue<UpdateUnit>();
        Timer timer = new Timer("Updater");
        timer.schedule(new UpdaterTask(), 0, periodMs);
    }

    public synchronized int getUpdateQueueSize() {
        return queue.size();
    }

    public synchronized void addResourceToTheUpdateQueue(Reference jobReference, String resource) {
        addResourceToTheUpdateQueue(jobReference, ResourcesHandler.getResources().get(resource));
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

            try {

                TasksResults tasksResults = scheduler.getAllTaskResults(u.reference.getId());
                OcciTasksResults occiTaskResults = new OcciTasksResults(tasksResults);


                // Create new category instances if required
                List<CreateInstanceRequest> createRequests = occiTaskResults.getCreateInstanceRequests();
                for (CreateInstanceRequest request: createRequests) {
                    try {
                        String location = createAnotherCategoryInstance(request);
                        occiTaskResults.add(
                                new UpdateAttributeRequest(
                                        request.getAttributeToUpdateWithLocation(), location));
                    } catch (Exception e) {
                        occiTaskResults.add(
                                new FailedRequest(e.getMessage()));
                    }
                }

                // Update categories' instances if required
                List<UpdateInstanceRequest> updateRequests = occiTaskResults.getUpdateInstanceRequests();
                for (UpdateInstanceRequest request: updateRequests) {
                    updateAnotherCategoryInstance(request);
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
            logger.debug("Not implemented yet.");
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

}
