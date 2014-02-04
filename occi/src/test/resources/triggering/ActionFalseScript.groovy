package triggering

import org.ow2.proactive.brokering.triggering.Action
import unittests.CatalogTest

public class ActionFalseScript implements Action {

    @Override
    void execute(Map<String, String> args) {
        CatalogTest.addOneFalseActionExecuted();
    }
}