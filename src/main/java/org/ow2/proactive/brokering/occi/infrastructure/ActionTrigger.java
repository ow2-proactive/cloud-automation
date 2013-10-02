package org.ow2.proactive.brokering.occi.infrastructure;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyRuntimeException;
import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.Reference;
import org.ow2.proactive.brokering.References;
import org.ow2.proactive.brokering.occi.Attribute;
import org.ow2.proactive.brokering.triggering.Action;
import org.ow2.proactive.brokering.triggering.Condition;
import org.ow2.proactive.brokering.utils.HttpUtility;

import java.util.*;

public class ActionTrigger {

    public static final String OCCI_MONITORING_PERIODMS = "occi.monitoring.periodms";
    public static final String OCCI_CONDITION_SCRIPT = "occi.monitoring.condition";
    public static final String OCCI_MONITORING_TRUEACTION = "occi.monitoring.trueaction";
    public static final String OCCI_MONITORING_FALSEACTION = "occi.monitoring.falseaction";
    public static final String OCCI_MONITORING_ACTION = "occi.monitoring.action";

    private static final String CLASSNAME = ".classname";
    public static final String OCCI_CONDITION_SCRIPT_CLASSNAME = OCCI_CONDITION_SCRIPT + CLASSNAME;
    public static final String OCCI_MONITORING_TRUEACTION_CLASSNAME = OCCI_MONITORING_TRUEACTION + CLASSNAME;
    public static final String OCCI_MONITORING_FALSEACTION_CLASSNAME = OCCI_MONITORING_FALSEACTION + CLASSNAME;
    public static final String OCCI_MONITORING_ACTION_CLASSNAME = OCCI_MONITORING_ACTION + CLASSNAME;

    public static final String OCCI_CORE_ID = "occi.core.id";

    private static final Logger logger = Logger.getLogger(ActionTrigger.class.getName());

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

        attributeList.add(new Attribute(OCCI_CONDITION_SCRIPT_CLASSNAME, mutable, required));
        attributeList.add(new Attribute(OCCI_MONITORING_ACTION_CLASSNAME, mutable, !required));
        attributeList.add(new Attribute(OCCI_MONITORING_FALSEACTION_CLASSNAME, mutable, !required));
        attributeList.add(new Attribute(OCCI_MONITORING_TRUEACTION_CLASSNAME, mutable, !required));

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

    // INNER CLASSES

    class ActionExecutor extends Thread {

        private Class action;
        private Map<String, String> args;

        public ActionExecutor(Map<String, String> args) throws ScriptException {
            this.args = args;
            this.action = ScriptUtils.getEncodedScriptAsClass(args, OCCI_MONITORING_ACTION);
        }

        private void executeAction(Class script) {
            if (script == null)
                return;

            try {
                Action action = (Action) script.newInstance();
                action.execute(args);
            } catch (Throwable e) {
                logger.warn("Error executing action: " + script, e);
            }
        }

        @Override
        public void run() {
            executeAction(action);
        }

    }

    class ConditionChecker extends TimerTask {

        private Map<String, String> args;
        private Class conditionScript;
        private Class actionCaseTrue;
        private Class actionCaseFalse;

        public ConditionChecker(Map<String, String> args) throws ScriptException {
            conditionScript = ScriptUtils.getEncodedScriptAsClass(args, OCCI_CONDITION_SCRIPT);
            actionCaseTrue = ScriptUtils.getEncodedScriptAsClass(args, OCCI_MONITORING_TRUEACTION);
            actionCaseFalse = ScriptUtils.getEncodedScriptAsClass(args, OCCI_MONITORING_FALSEACTION);
            this.args = args;
            if (conditionScript == null || actionCaseTrue == null)
                throw new ScriptException("Condition and True scripts must be provided");
        }

        @Override
        public void run() {
            if (checkCondition(conditionScript))
                executeAction(actionCaseTrue);
            else
                executeAction(actionCaseFalse);
        }

        private Boolean checkCondition(Class script) {
            try {
                Condition cond = (Condition) script.newInstance();
                return cond.evaluate(args);
            } catch (Throwable e) {
                logger.warn("Error when checking condition: " + script, e);
                return false;
            }
        }

        private void executeAction(Class script) {
            if (script == null)
                return;

            try {
                Action cond = (Action) script.newInstance();
                cond.execute(args);
            } catch (Throwable e) {
                logger.warn("Error when executing action: " + script, e);
            }
        }

    }

    static class ScriptUtils {

        public static Class getEncodedScriptAsClass(Map<String, String> args, String key)
                throws ScriptException {

            GroovyClassLoader gcl = new GroovyClassLoader();
            String scriptClassname = args.get(key + CLASSNAME);
            String scriptEncoded = args.get(key);

            if (isClassName(scriptClassname)) {
                try {
                    return Class.forName(scriptClassname);
                } catch (ClassNotFoundException e) {
                    throw new ScriptException(e);
                }
            } else if (encodedScriptIsNotEmpty(scriptEncoded))
                try {
                    return gcl.parseClass(HttpUtility.decodeBase64(scriptEncoded));
                } catch (GroovyRuntimeException e) {
                    throw new ScriptException(e);
                }
            else
                return null;

        }

        public static boolean encodedScriptIsNotEmpty(String encodedScript) {
            return (encodedScript != null && !encodedScript.isEmpty());
        }

        public static boolean isClassName(String className) {
            return (className != null && className.contains("."));
        }

    }

    static class ScriptException extends Exception {

        public ScriptException(String message) {
            super(message);
        }

        public ScriptException(String message, Throwable throwable) {
            super(message, throwable);
        }

        public ScriptException(Throwable throwable) {
            super(throwable);
        }

    }
}
