package conditions

import org.ow2.proactive.brokering.triggering.Condition

public class ConditionScript extends Condition {

    private static boolean last = false;

    public Boolean evaluate(Map<String, String> args) {
        last = !last;
        return last;
    }
}