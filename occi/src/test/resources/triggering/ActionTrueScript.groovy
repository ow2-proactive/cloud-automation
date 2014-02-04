package triggering

import org.ow2.proactive.brokering.triggering.Action
import unittests.CatalogTest

public class ActionTrueScript implements Action {

    @Override
    void execute(Map<String, String> args) {
        CatalogTest.addOneTrueActionExecuted();
    }
}