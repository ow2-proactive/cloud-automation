package org.ow2.proactive.brokering;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.ow2.proactive.brokering.occi.OcciServer;
import org.ow2.proactive.brokering.occi.Resource;
import org.ow2.proactive.brokering.occi.api.Occi;
import org.ow2.proactive.brokering.occi.categories.Utils;
import org.ow2.proactive.workflowcatalog.Reference;
import org.ow2.proactive.workflowcatalog.utils.HttpUtility;
import org.ow2.proactive.workflowcatalog.utils.scheduling.JobNotFinishedException;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerProxy;
import org.apache.log4j.Logger;

public class Updater {

    private static final Logger logger = Logger.getLogger(Updater.class.getName());

    private Occi occi;
    private SchedulerProxy scheduler;
    private LinkedBlockingQueue<UpdateUnit> queue;

    public Updater(SchedulerProxy scheduler, Long periodMs) {
        this(new OcciServer(), scheduler, periodMs);
    }

    public Updater(Occi occi, SchedulerProxy scheduler, Long periodMs) {
        this.occi = occi;
        this.scheduler = scheduler;
        this.queue = new LinkedBlockingQueue<UpdateUnit>();
        Timer timer = new Timer();
        timer.schedule(new UpdaterTask(), 0, periodMs);
    }

    public synchronized int getUpdateQueueSize() {
        return queue.size();
    }

    public synchronized void addResourceToTheUpdateQueue(Reference jobReference, String resource) {
        addResourceToTheUpdateQueue(jobReference, Resource.getResources().get(resource));
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

                try {
                    updateOcciResource(u);
                } catch (Throwable e) {
                    logger.warn("Error updating: " + u, e);
                }

            }

        }

        private void updateOcciResource(UpdateUnit u) {
            try {
                Map<String, String> taskResults = scheduler.getAllTaskResults(u.reference.getId());
                String attributes = flattenTaskResultsAndConvertJsonTaskResultsToMap(taskResults);

                Response r = occi.updateResource(
                        u.resource.getCategory(), u.resource.getUuid(),
                        null, attributes);

                printLogsIfIncorrectExecution(r);

                removeUpdateItem(u);

            } catch (JobNotFinishedException e) {
                logger.debug("Waiting for update job: " + u);
            } catch (Exception e) {
                logger.warn("Unexpected error while updating: " + u, e);
                removeUpdateItem(u);
            }

        }

        private void printLogsIfIncorrectExecution(Response r) {
            if (HttpUtility.isNotSuccessStatusCode(r.getStatus()))
                logger.warn("Error trying to update category instance: " + r);
        }

        private void removeUpdateItem(UpdateUnit u) {
            logger.debug("Removing '" + u + "' from update queue.");
            queue.remove(u);
        }

        private String flattenTaskResultsAndConvertJsonTaskResultsToMap(
                Map<String, String> allProperties) {

            Map<String, String> flattenTaskResults = new HashMap<String, String>();
            StringBuilder errorsString = new StringBuilder();
            for (Map.Entry<String, String> taskEntry : allProperties.entrySet()) {
                String taskName = taskEntry.getKey();
                String taskResult = taskEntry.getValue();

                try {
                    JsonObject taskResultJson = Utils.convertToJson(taskResult);
                    flattenTaskResults.putAll(Utils.convertToMap(taskResultJson));
                } catch (Exception e) {
                    errorsString.append(taskName);
                    errorsString.append(": '");
                    errorsString.append(taskResult);
                    errorsString.append("'\n");
                }
            }
            flattenTaskResults.put("occi.error.description", // TODO improve attribute rendering on OCCIServer (use json)
                                   Utils.escapeAttribute(errorsString.toString()));
            return Utils.buildString(flattenTaskResults);
        }
    }

}
