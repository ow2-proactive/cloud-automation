package org.ow2.proactive.brokering;

import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.occi.OcciServer;
import org.ow2.proactive.brokering.occi.Resource;
import org.ow2.proactive.brokering.occi.api.Occi;
import org.ow2.proactive.brokering.occi.infrastructure.Utils;
import org.ow2.proactive.brokering.utils.HttpUtility;
import org.ow2.proactive.brokering.utils.scheduling.JobNotFinishedException;
import org.ow2.proactive.brokering.utils.scheduling.SchedulerProxy;

import javax.json.JsonObject;
import javax.json.stream.JsonParsingException;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Updater {

    private static final Logger logger = Logger.getLogger(Updater.class.getName());

    private Occi occi;
    private Timer timer;
    private SchedulerProxy scheduler;
    private LinkedBlockingQueue<UpdateUnit> queue;

    public Updater(SchedulerProxy scheduler, Long periodMs) {
        this(OcciServer.getInstance(), scheduler, periodMs);
    }

    public Updater(Occi occi, SchedulerProxy scheduler, Long periodMs) {
        this.occi = occi;
        this.scheduler = scheduler;
        this.queue = new LinkedBlockingQueue<UpdateUnit>();
        this.timer = new Timer();
        this.timer.schedule(new UpdaterTask(), 0, periodMs);
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

                JsonObject taskResults = scheduler.getAllTaskResultsAsJson(u.reference);
                String attributes = extractAllJsonAttributesFromAllJsonTaskResults(taskResults);

                Response r = occi.updateResource(
                        u.resource.getCategory(), u.resource.getUuid().toString(),
                        null, attributes);

                printLogsIfIncorrectExecution(r);

                removeUpdateItem(u);

            } catch (JsonParsingException e) {
                removeUpdateItem(u);
            } catch (JobNotFinishedException e) {
                logger.debug("Waiting for update job:" + u.reference);
            } catch (Exception e) {
                logger.warn("Unexpected error getting info to update category instance", e);
                removeUpdateItem(u);
            }

        }

        private void printLogsIfIncorrectExecution(Response r) {
            if (!HttpUtility.isSuccessStatusCode(r.getStatus()))
                logger.warn("Error trying to update category instance: " + r);
        }

        private void removeUpdateItem(UpdateUnit u) {
            logger.debug("Removing '" + u.resource + " from queue.");
            queue.remove(u);
        }

        private String extractAllJsonAttributesFromAllJsonTaskResults(JsonObject taskResults) throws JsonParsingException {
            Map<String, String> allProperties = new HashMap<String, String>();
            for (String taskName : taskResults.keySet()) {
                String aTaskResult = taskResults.getString(taskName);
                JsonObject taskResultProperties = Utils.convertToJson(aTaskResult);
                allProperties.putAll(Utils.convertToMap(taskResultProperties));
            }
            return Utils.buildString(allProperties);
        }
    }

}
