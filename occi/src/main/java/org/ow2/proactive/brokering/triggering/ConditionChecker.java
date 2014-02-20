package org.ow2.proactive.brokering.triggering;


import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import static org.ow2.proactive.brokering.occi.categories.trigger.ActionTrigger.*;

public class ConditionChecker extends TimerTask {

    private static final Logger logger = Logger.getLogger(ConditionChecker.class.getName());

    private Map<String, String> args;
    private Class conditionScript;
    private Class actionCaseTrue;
    private Class actionCaseFalse;

    public ConditionChecker(Map<String, String> args) throws ScriptException {
        try {
            conditionScript = ScriptUtils.getEncodedScriptAsClass(args, OCCI_CONDITION_SCRIPT);
            actionCaseTrue = ScriptUtils.getEncodedScriptAsClass(args, OCCI_MONITORING_TRUEACTION);
            if (isScriptProvided(args, OCCI_MONITORING_FALSEACTION))
                actionCaseFalse = ScriptUtils.getEncodedScriptAsClass(args, OCCI_MONITORING_FALSEACTION);
        } catch (ScriptException e) {
            logger.info("Bad script", e);
        }

        this.args = args;
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
            return cond.evaluate(new HashMap<String,String>(args));
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
            cond.execute(new HashMap<String, String>(args));
        } catch (Throwable e) {
            logger.warn("Error when executing action: " + script, e);
        }
    }

    private boolean isScriptProvided(Map<String, String> args, String key) {
        return args.containsKey(key) && !args.get(key).isEmpty();
    }

}

