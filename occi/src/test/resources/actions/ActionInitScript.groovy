package actions

import org.ow2.proactive.brokering.occi.categories.trigger.ActionTriggerTest
import org.ow2.proactive.brokering.triggering.Action

public class ActionInitScript extends Action {

    @Override
    void execute(Map<String, String> args) {
        ActionTriggerTest.addOneInitActionExecuted();
    }
}