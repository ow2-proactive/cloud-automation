package config.actions

import org.ow2.proactive.brokering.triggering.Action

public class ScaleOutScript implements Action {

    @Override
    void execute(Map<String, String> args) {
        System.out.println("Scale out!")
    }
}