package org.ow2.proactive.brokering.triggering

public class ActionFalseScript implements Action {

    @Override
    void execute(Map<String, String> args) {
        ActionTriggerTest.addOneFalseActionExecuted();
    }
}