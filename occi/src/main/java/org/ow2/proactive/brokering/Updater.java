package org.ow2.proactive.brokering;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import javax.json.stream.JsonParsingException;
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

    public synchronized void addResourceToTheUpdateQueue(Reference jobReference, UUID resource) {
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
                    logger.warn("Error updating", e);
                }

            }

        }

        private void updateOcciResource(UpdateUnit u) {
            try {
                Map<String, String> taskResults = scheduler.getAllTaskResults(u.reference);
                String attributes = flattenTaskResultsAndConvertJsonTaskResultsToMap(taskResults);

                Response r = occi.updateResource(
                        u.resource.getCategory(), u.resource.getUuid().toString(),
                        null, attributes);

                printLogsIfIncorrectExecution(r);

                removeUpdateItem(u);

            } catch (JsonParsingException e) {
                logger.debug("Could not parse json: " + u.reference);
                removeUpdateItem(u);
            } catch (JobNotFinishedException e) {
                logger.debug("Waiting for update job:" + u.reference);
            } catch (Exception e) {
                logger.warn("Unexpected error getting info to update category instance", e);
                removeUpdateItem(u);
            }

        }

        private void printLogsIfIncorrectExecution(Response r) {
            if (HttpUtility.isNotSuccessStatusCode(r.getStatus()))
                logger.warn("Error trying to update category instance: " + r);
        }

        private void removeUpdateItem(UpdateUnit u) {
            logger.debug("Removing '" + u.resource + " from queue.");
            queue.remove(u);
        }

        private String flattenTaskResultsAndConvertJsonTaskResultsToMap(
          Map<String, String> allProperties) throws JsonParsingException {
            Map<String, String> flattenTaskResults = new HashMap<String, String>();
            for (Map.Entry<String, String> mapEntry : allProperties.entrySet()) {
                flattenTaskResults.putAll(Utils.convertToMap(Utils.convertToJson(mapEntry.getValue())));
            }
            return Utils.buildString(flattenTaskResults);
        }
    }

}
