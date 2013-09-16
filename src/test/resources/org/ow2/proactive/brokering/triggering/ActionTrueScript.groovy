package org.ow2.proactive.brokering.triggering

public class ActionTrueScript implements Action {

    @Override
    void execute(Map<String, String> args) {
        ActionTriggerTest.addOneTrueActionExecuted();
    }
}