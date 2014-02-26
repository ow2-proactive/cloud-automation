package unittests.actions

import org.ow2.proactive.brokering.triggering.Action
import unittests.ActionTriggerTest

public class ActionTrueScript extends Action {

    @Override
    void execute(Map<String, String> args) {
        ActionTriggerTest.addOneTrueActionExecuted();
    }
}