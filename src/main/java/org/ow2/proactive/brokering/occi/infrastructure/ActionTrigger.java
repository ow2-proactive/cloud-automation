package org.ow2.proactive.brokering.occi.infrastructure;

import org.ow2.proactive.brokering.Reference;
import org.ow2.proactive.brokering.References;
import org.ow2.proactive.brokering.occi.Attribute;
import org.ow2.proactive.brokering.triggering.ActionExecutor;
import org.ow2.proactive.brokering.triggering.ConditionChecker;
import org.ow2.proactive.brokering.triggering.ScriptException;

import java.util.*;

public class ActionTrigger {

    public static final String OCCI_MONITORING_PERIODMS = "occi.monitoring.periodms";
    public static final String OCCI_CONDITION_SCRIPT = "occi.monitoring.condition";
    public static final String OCCI_MONITORING_TRUEACTION = "occi.monitoring.trueaction";
    public static final String OCCI_MONITORING_FALSEACTION = "occi.monitoring.falseaction";
    public static final String OCCI_MONITORING_ACTION = "occi.monitoring.action";

    public static final String OCCI_CORE_ID = "occi.core.id";

    public static Map<String, Timer> timers;

    private static ActionTrigger instance;

    private ActionTrigger() {
        timers = new HashMap<String, Timer>();
    }

    public static ActionTrigger getInstance() {
        // TODO : Double check locking
        if (instance == null) {
            instance = new ActionTrigger();
        }
        return instance;
    }

    public List<Attribute> getSpecificAttributeList() {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        boolean mutable = true;
        boolean required = true;
        attributeList.add(new Attribute(OCCI_MONITORING_PERIODMS, mutable, !required));

        attributeList.add(new Attribute(OCCI_CONDITION_SCRIPT, mutable, required));
        attributeList.add(new Attribute(OCCI_MONITORING_ACTION, mutable, !required));
        attributeList.add(new Attribute(OCCI_MONITORING_FALSEACTION, mutable, !required));
        attributeList.add(new Attribute(OCCI_MONITORING_TRUEACTION, mutable, !required));

        return attributeList;
    }

    public static Map<String, Timer> getTimers() {
        return timers;
    }

    public References request(
            String category,
            String operation,
            String action,
            Map<String, String> attributes) {

        References references = new References();
        if ("create".equalsIgnoreCase(operation)) {
            if ("scheduleonce".equalsIgnoreCase(action)) {
                references.add(createOneShotActionTrigger(attributes));
            } else {
                references.add(createPeriodicActionTrigger(attributes));
            }
        } else if ("delete".equalsIgnoreCase(operation)) {
            references.add(removeActionTrigger(attributes));
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
            conditionChecker = new ConditionChecker(attributes);
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

    private Reference createOneShotActionTrigger(Map<String, String> attributes) {

        ActionExecutor actionExecutor = null;
        String uuid = null;
        try {
            actionExecutor = new ActionExecutor(attributes);
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
