package unittests.triggering

import org.ow2.proactive.brokering.triggering.Action
import unittests.ActionTriggerTest

public class ActionTrueScript implements Action {

    @Override
    void execute(Map<String, String> args) {
        ActionTriggerTest.addOneTrueActionExecuted();
    }
}