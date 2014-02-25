package org.ow2.proactive.brokering.triggering;

import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.occi.categories.trigger.ActionTrigger;
import java.util.Map;

public class ActionExecutor extends Thread {

    private static final Logger logger = Logger.getLogger(ActionExecutor.class.getName());

    private Class action;
    private Map<String, String> args;

    public ActionExecutor(Map<String, String> args, Actions actions) throws ScriptException {
        this.args = args;
        this.action = ScriptUtils.getEncodedScriptAsClass(args, actions, ActionTrigger.OCCI_MONITORING_ACTION);
    }

    private void executeAction(Class script) {
        if (script == null)
            return;

        try {
            Action action = (Action) script.newInstance();
            action.execute(args);
        } catch (Throwable e) {
            logger.warn("Error executing action: " + e.getMessage());
            logger.debug("Error executing action: " + script, e);
        }
    }

    @Override
    public void run() {
        executeAction(action);
    }

}
