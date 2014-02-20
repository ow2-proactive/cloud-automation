package triggering

import org.ow2.proactive.brokering.triggering.Condition

public class ConditionScript implements Condition {

    private static boolean last = false;

    public boolean evaluate(Map<String, String> args) {
        last = !last;
        return last;
    }
}