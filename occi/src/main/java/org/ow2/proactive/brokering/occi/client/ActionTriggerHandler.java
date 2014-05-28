package org.ow2.proactive.brokering.occi.client;

import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.Configuration;
import org.ow2.proactive.brokering.occi.categories.Utils;
import org.ow2.proactive.brokering.occi.categories.trigger.ActionTrigger;
import org.ow2.proactive.brokering.triggering.*;
import org.ow2.proactive.workflowcatalog.Reference;
import org.ow2.proactive.workflowcatalog.References;

import javax.xml.bind.JAXBException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import static org.ow2.proactive.brokering.occi.Resource.OP_CREATE;
import static org.ow2.proactive.brokering.occi.Resource.OP_UPDATE;
import static org.ow2.proactive.brokering.occi.categories.trigger.ActionTrigger.OCCI_CORE_ID;
import static org.ow2.proactive.brokering.occi.categories.trigger.ActionTrigger
        .OCCI_MONITORING_PERIODMS;

public class ActionTriggerHandler {

    private static Logger logger = Logger.getLogger(ActionTriggerHandler.class);

    public static final String ACTION_SCHEDULE_ONCE = "scheduleonce";
    public static final String ACTION_SCHEDULE = "schedule";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_STOP = "stop";

    private static Map<String, Timer> timers;
    private static Actions actions;
    private static Conditions conditions;

    private static ActionTriggerHandler instance;

    private ActionTriggerHandler(Configuration config) {
        timers = new HashMap<String, Timer>();
        actions = new Actions(
                Utils.getScriptsPath(config.actions.path, "config/actions"),
                config.actions.refresh);
        conditions = new Conditions(
                Utils.getScriptsPath(config.conditions.path, "config/conditions"),
                config.conditions.refresh);
    }

    private ActionTriggerHandler() throws JAXBException {
        this(Utils.getConfiguration());
    }

    public static ActionTriggerHandler getInstance() {
        if (instance == null) {
            try {
                instance = new ActionTriggerHandler();
            } catch (JAXBException e) {
                throw new RuntimeException("Cannot create action trigger", e);
            }
        }
        return instance;
    }

    public static ActionTriggerHandler getInstance(Configuration config) {
        if (instance == null) {
            instance = new ActionTriggerHandler(config);
        }
        return instance;
    }

    public static Map<String, Timer> getTimers() {
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
