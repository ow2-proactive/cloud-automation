package org.ow2.proactive.brokering.occi.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import org.ow2.proactive.brokering.occi.categories.Utils;
import org.ow2.proactive.brokering.occi.categories.trigger.ActionTrigger;
import org.ow2.proactive.brokering.triggering.ActionExecutor;
import org.ow2.proactive.brokering.triggering.Actions;
import org.ow2.proactive.brokering.triggering.ConditionChecker;
import org.ow2.proactive.brokering.triggering.Conditions;
import org.ow2.proactive.brokering.triggering.ScriptException;
import org.ow2.proactive.workflowcatalog.Reference;
import org.ow2.proactive.workflowcatalog.References;
import org.apache.log4j.Logger;

import static org.ow2.proactive.brokering.occi.Resource.OP_CREATE;
import static org.ow2.proactive.brokering.occi.Resource.OP_UPDATE;
import static org.ow2.proactive.brokering.occi.categories.trigger.ActionTrigger.OCCI_CORE_ID;
import static org.ow2.proactive.brokering.occi.categories.trigger.ActionTrigger.OCCI_MONITORING_PERIODMS;

public class ActionTriggerHandler {

    private static Logger logger = Logger.getLogger(ActionTriggerHandler.class);

    public static final String ACTION_SCHEDULE_ONCE = "scheduleonce";
    public static final String ACTION_SCHEDULE = "schedule";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_STOP = "stop";

    private Map<String, Timer> timers;
    private Actions actions;
    private Conditions conditions;

    public ActionTriggerHandler(String actionsPath, long actionsRefresh, String conditionsPath,
      long conditionsRefresh) {
        timers = new HashMap<String, Timer>();
        actions = new Actions(
                Utils.getScriptsPath(actionsPath, "/config/actions"),
                actionsRefresh);
        conditions = new Conditions(
                Utils.getScriptsPath(conditionsPath, "/config/conditions"),
                conditionsRefresh);
    }

    public Map<String, Timer> getTimers() {
        return timers;
    }

    public References request(
      String operation,
      String action,
      Map<String, String> attributes) {

        References references = new References();
        if (OP_CREATE.equalsIgnoreCase(operation)) {
            if (ACTION_SCHEDULE_ONCE.equalsIgnoreCase(action)) {
                references.add(createOneShotActionTrigger(attributes, ActionTrigger.OCCI_MONITORING_ACTION));
            } else {
                references.add(createOneShotActionTrigger(attributes, ActionTrigger.OCCI_MONITORING_INITACTION));
                references.add(createPeriodicActionTrigger(attributes));
            }
        } else if (OP_UPDATE.equalsIgnoreCase(operation)) {
            if (ACTION_DELETE.equalsIgnoreCase(action) || ACTION_STOP.equalsIgnoreCase(action)) {
                references.add(createOneShotActionTrigger(attributes, ActionTrigger.OCCI_MONITORING_STOPACTION));
                references.add(removeActionTrigger(attributes));
            }
        }
        return references;
    }

    private Reference removeActionTrigger(Map<String, String> attributes) {
        Timer timer = timers.remove(getUuid(attributes));
        timer.cancel();
        return Reference.buildActionTriggerReference(
                "Timer removed", getUuid(attributes));
    }

    private Reference createPeriodicActionTrigger(Map<String, String> attributes) {
        ConditionChecker conditionChecker = null;

        Long delay = null;
        String uuid = null;

        try {
            conditionChecker = new ConditionChecker(attributes, actions, conditions);
            delay = getDelay(attributes);
            uuid = getUuid(attributes);
        } catch (ScriptException e) {
            return Reference.buildActionTriggerFailedReference(
                    "Bad script", e);
        } catch (IllegalArgumentException e) {
            return Reference.buildActionTriggerFailedReference(
                    "Bad arguments", e);
        }

        Timer timer = new Timer();
        timer.schedule(conditionChecker, 0, delay);
        timers.put(uuid, timer);
        return Reference.buildActionTriggerReference(
                "Timer created", uuid);
    }

    private Reference createOneShotActionTrigger(Map<String, String> attributes, String key) {

        ActionExecutor actionExecutor = null;
        String uuid = null;
        try {
            actionExecutor = new ActionExecutor(attributes, actions, key);
            uuid = getUuid(attributes);
        } catch (ScriptException e) {
            return Reference.buildActionTriggerFailedReference(
                    "Bad script", e);
        } catch (IllegalArgumentException e) {
            return Reference.buildActionTriggerFailedReference(
                    "Bad arguments", e);
        }

        actionExecutor.start();
        return Reference.buildActionTriggerReference(
                "Action executor created", uuid);
    }


    private String getUuid(Map<String, String> attributes) throws IllegalArgumentException {
        String str = attributes.get(OCCI_CORE_ID);
        if (str == null)
            throw new IllegalArgumentException("uuid cannot be null");
        return str;
    }

    private long getDelay(Map<String, String> atts) throws IllegalArgumentException {
        String str = atts.get(OCCI_MONITORING_PERIODMS);
        if (str == null)
            throw new IllegalArgumentException("delay cannot be null");

        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
