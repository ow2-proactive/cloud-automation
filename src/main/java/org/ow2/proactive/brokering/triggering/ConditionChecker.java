package org.ow2.proactive.brokering.triggering;


import org.apache.log4j.Logger;

import java.util.Map;
import java.util.TimerTask;

import static org.ow2.proactive.brokering.occi.infrastructure.ActionTrigger.*;

public class ConditionChecker extends TimerTask {

    private static final Logger logger = Logger.getLogger(ConditionChecker.class.getName());

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

