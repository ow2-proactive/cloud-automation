package org.ow2.proactive.brokering.occi.infrastructure;

import groovy.lang.GroovyClassLoader;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.Reference;
import org.ow2.proactive.brokering.References;
import org.ow2.proactive.brokering.occi.Attribute;
import org.ow2.proactive.brokering.triggering.Action;
import org.ow2.proactive.brokering.triggering.Condition;

import java.util.*;

public class ActionTrigger {

    public static final String OCCI_CONDITION_SCRIPT = "occi.monitoring.condition";
    public static final String OCCI_MONITORING_PERIODMS = "occi.monitoring.periodms";
    public static final String OCCI_MONITORING_TRUEACTION = "occi.monitoring.trueaction";
    public static final String OCCI_MONITORING_FALSEACTION = "occi.monitoring.falseaction";
    public static final String OCCI_MONITORING_ACTION = "occi.monitoring.action";

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
        attributeList.add(new Attribute(OCCI_CONDITION_SCRIPT, mutable, required));
        attributeList.add(new Attribute(OCCI_MONITORING_PERIODMS, mutable, !required));
        attributeList.add(new Attribute(OCCI_MONITORING_FALSEACTION, mutable, !required));
        attributeList.add(new Attribute(OCCI_MONITORING_TRUEACTION, mutable, !required));
        return attributeList;
    }

    public References request(
            String category,
            String operation,
            String action,
            Map<String, String> attributes) {

        References references = new References();
        if ("create".equalsIgnoreCase(operation)) {
            if ("scheduleonce".equalsIgnoreCase(action)) {
                ActionExecutor actionExecutor = new ActionExecutor(attributes);
                actionExecutor.start();
                references.add(
                        Reference.buildActionTriggerReference(
                                "Action executor created", getUuid(attributes)));
            } else {
                ConditionChecker conditionChecker = new ConditionChecker(attributes);
                Timer timer = new Timer();
                timer.schedule(conditionChecker, 0, getDelay(attributes));
                timers.put(getUuid(attributes), timer);
                references.add(
                        Reference.buildActionTriggerReference(
                                "Timer created", getUuid(attributes)));
            }
        } else if ("delete".equalsIgnoreCase(operation)) {
            Timer timer = timers.remove(getUuid(attributes));
            timer.cancel();
            references.add(
                    Reference.buildActionTriggerReference(
                            "Timer removed", getUuid(attributes)));
        }
        return references;
    }

    private String getUuid(Map<String, String> attributes) {
        return attributes.get("occi.core.id");
    }

    private long getDelay(Map<String, String> atts) {
        return Long.parseLong(atts.get(OCCI_MONITORING_PERIODMS));
    }

    public static Map<String, Timer> getTimers() {
        return timers;
    }

    public static String encodeBase64(String src) {
        return new String(Base64.encodeBase64(src.getBytes()));
    }

    public static String decodeBase64(String src) {
        return new String(Base64.decodeBase64(src.getBytes()));
    }

    // INNER CLASSES

    class ActionExecutor extends Thread {

        private Class action;
        private Map<String, String> args;

        public ActionExecutor(Map<String, String> args) {
            this.args = args;
            this.action = getActionAsClass(args, OCCI_MONITORING_ACTION);
        }

        private Class getActionAsClass(Map<String, String> args, String key) {
            GroovyClassLoader gcl = new GroovyClassLoader();
            String encodedScript = args.get(key);
            if (encodedScript != null)
                return gcl.parseClass(decodeBase64(encodedScript));
            else
                return null;
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

        public ConditionChecker(Map<String, String> args) {
            this.conditionScript = getConditionScript(args);
            this.actionCaseTrue = getActionAsClass(args, OCCI_MONITORING_TRUEACTION);
            this.actionCaseFalse = getActionAsClass(args, OCCI_MONITORING_FALSEACTION);
            this.args = args;
        }

        private Class getConditionScript(Map<String, String> args) {
            GroovyClassLoader gcl = new GroovyClassLoader();
            String encodedScript = args.get(OCCI_CONDITION_SCRIPT);
            Class clazz = gcl.parseClass(decodeBase64(encodedScript));
            return clazz;
        }

        private Class getActionAsClass(Map<String, String> args, String key) {
            GroovyClassLoader gcl = new GroovyClassLoader();
            String encodedScript = args.get(key);
            if (encodedScript != null)
                return gcl.parseClass(decodeBase64(encodedScript));
            else
                return null;
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
}
