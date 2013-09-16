package org.ow2.proactive.brokering;

import org.apache.http.auth.AuthenticationException;
import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.occi.Resource;
import org.ow2.proactive.brokering.occi.api.Occi;
import org.ow2.proactive.brokering.occi.infrastructure.Utils;
import org.ow2.proactive.brokering.utils.HttpUtility;
import org.ow2.proactive.brokering.utils.scheduling.SchedulerProxy;

import javax.json.JsonObject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Updater {

    private static final Logger logger = Logger.getLogger(Updater.class.getName());

    private static final Long DEFAULT_PERIOD_MS = TimeUnit.SECONDS.toMillis(3);

    private Occi occi;
    private Timer timer;
    private SchedulerProxy scheduler;
    private LinkedBlockingQueue<UpdateUnit> queue;

    public Updater(Occi occi, SchedulerProxy scheduler, Long periodMs) {
        this.occi = occi;
        this.scheduler = scheduler;
        queue = new LinkedBlockingQueue<UpdateUnit>();
        timer = new Timer("occi-job-updater");
        timer.schedule(new UpdaterTask(), 0, periodMs);
    }


    public synchronized void addResourceToTheUpdateQueue(Reference jobReference, Resource resource) {
        try {
            queue.put(new UpdateUnit(resource, jobReference));
        } catch (InterruptedException e) {
            logger.warn("Not expected.", e);
        }
    }

    class UpdaterTask extends TimerTask {
        @Override
        public void run() {
            UpdateUnit u = queue.peek();

            if (u == null)
                return;

            try {
                updateOcciResource(u);
            } catch (Throwable e) {
                logger.debug(e);
            }
        }

        private void updateOcciResource(UpdateUnit u) {
            JsonObject taskResults = null;
            try {
                taskResults = scheduler.getAllTaskResultsAsJson(u.reference);
            } catch (AuthenticationException e) {
                logger.warn("Could not authenticate.", e);
            } catch (IOException e) {
                logger.warn("Error getting job result.", e);
            }

            String atts =  extractJsonAttributesFromTaskResults(taskResults);
            Response r = occi.updateResource(u.resource.getCategory(), u.resource.getUuid().toString(), null, atts);

            if (HttpUtility.isSuccessStatusCode(r.getStatus())) {
                logger.info("Removing '" + u.resource + " from queue.");
                queue.remove(u);
            }

        }

        private String extractJsonAttributesFromTaskResults(JsonObject taskResults) {
            Map<String, String> allProperties = new HashMap<String, String>();
            for (String taskName : taskResults.keySet()) {
                String aTaskResult = taskResults.getString(taskName);
                JsonObject taskResultProperties = Utils.convertToJson(aTaskResult);
                allProperties.putAll(Utils.convertToMap(taskResultProperties));
            }
            return Utils.buildString(allProperties);
        }
    }

    class UpdateUnit {
        Reference reference;
        Resource resource;
        UpdateUnit(Resource resource, Reference reference) {
            this.resource = resource;
            this.reference = reference;
        }
    }
}
