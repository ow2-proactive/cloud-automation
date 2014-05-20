package org.ow2.proactive.brokering.occi.categories.trigger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import javax.xml.bind.JAXBException;

import org.ow2.proactive.brokering.Configuration;
import org.ow2.proactive.brokering.occi.Attribute;
import org.ow2.proactive.brokering.occi.Category;
import org.ow2.proactive.brokering.occi.categories.Utils;
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

public class ActionTrigger implements Category {

    private static Logger logger = Logger.getLogger(ActionTrigger.class);

    public static final String OCCI_MONITORING_PERIODMS = "occi.monitoring.periodms";
    public static final String OCCI_CONDITION_SCRIPT = "occi.monitoring.condition";
    public static final String OCCI_MONITORING_INITACTION = "occi.monitoring.initaction";
    public static final String OCCI_MONITORING_TRUEACTION = "occi.monitoring.trueaction";
    public static final String OCCI_MONITORING_FALSEACTION = "occi.monitoring.falseaction";
    public static final String OCCI_MONITORING_STOPACTION = "occi.monitoring.stopaction";
    public static final String OCCI_MONITORING_ACTION = "occi.monitoring.action";
    public static final String OCCI_MONITORING_METADATA = "occi.monitoring.metadata";

    public static final String OCCI_CORE_ID = "occi.core.id";

    private static Map<String, Timer> timers;
    private static Actions actions;
    private static Conditions conditions;

    private File getPath(String path, String defaultPath) {
        File c = new File(path);
        if (!c.isDirectory()) {
            c = new File(ActionTrigger.class.getResource(defaultPath).getFile());
        }
        return c;
    }

    private static ActionTrigger instance;

    private ActionTrigger(Configuration config) {
        timers = new HashMap<String, Timer>();
        actions = new Actions(getPath(config.actions.path, "/config/actions"), config.actions.refresh);
        conditions = new Conditions(getPath(config.conditions.path, "/config/conditions"), config.conditions.refresh);
    }
    private ActionTrigger() throws JAXBException {
        this(Utils.getConfiguration());
    }

    public static ActionTrigger getInstance() {
        if (instance == null) {
            try {
                instance = new ActionTrigger();
            } catch (JAXBException e) {
                logger.error("Cannot create action trigger", e);
            }
        }
        return instance;
    }

    public static ActionTrigger getInstance(Configuration config) {
        if (instance == null) {
            instance = new ActionTrigger(config);
        }
        return instance;
    }

    @Override
    public List<Attribute> getSpecificAttributeList() {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        boolean mutable = true;
        boolean required = true;
        attributeList.add(new Attribute(OCCI_MONITORING_PERIODMS, mutable, !required));

        attributeList.add(new Attribute(OCCI_CONDITION_SCRIPT, mutable, required));
        attributeList.add(new Attribute(OCCI_MONITORING_ACTION, mutable, !required));
        attributeList.add(new Attribute(OCCI_MONITORING_FALSEACTION, mutable, !required));
        attributeList.add(new Attribute(OCCI_MONITORING_TRUEACTION, mutable, !required));
        attributeList.add(new Attribute(OCCI_MONITORING_METADATA, mutable, !required));

        return attributeList;
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
            if ("scheduleonce".equalsIgnoreCase(action)) {
                references.add(createOneShotActionTrigger(attributes, OCCI_MONITORING_ACTION));
            } else {
                references.add(createOneShotActionTrigger(attributes, OCCI_MONITORING_INITACTION));
                references.add(createPeriodicActionTrigger(attributes));
            }
        } else if (OP_UPDATE.equalsIgnoreCase(operation)) {
            if ("delete".equalsIgnoreCase(action) || "stop".equalsIgnoreCase(action)) {
                references.add(createOneShotActionTrigger(attributes, OCCI_MONITORING_STOPACTION));
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
