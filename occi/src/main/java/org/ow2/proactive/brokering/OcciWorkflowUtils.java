package org.ow2.proactive.brokering;

import java.util.Arrays;
import java.util.Map;

import org.ow2.proactive.workflowcatalog.Workflow;
import org.apache.log4j.Logger;

public class OcciWorkflowUtils {

    protected static final Logger logger = Logger.getLogger(OcciWorkflowUtils.class.getName());

    /**
     * Only checks compliance with the generic information.
     */
    public static boolean isCompliant(Workflow workflow, String category, String operation, String action, Map<String, String> attributes) {
        // Generic Information 'category' and 'operation' must contains given 'category' and 'operation' values
        if (!valueIsContainedInSet(category, workflow.getGenericInformation("category"))) {
            logger.debug(workflow.getName() + " : Wrong category (" + workflow.getGenericInformation("category") + ")");
            return false;
        }

       if( operation != null && !valueIsContainedInSet(operation, workflow.getGenericInformation("operation"))){
           logger.debug(workflow.getName() + " : Wrong operation (" + workflow.getGenericInformation("operation") + ")");
           return false;
       }

        // If an action is given, it must be present in the Generic information
        if (action != null && !valueIsContainedInSet(action, workflow.getGenericInformation("action"))) {
            logger.debug(workflow.getName() + " : Wrong action (" + action + "/" + workflow.getGenericInformation("action") + ")");
            return false;
        }

        // Request attributes which are in Generic Informations must matches (contains) their values
        for (String attributeKey : attributes.keySet()) {
            if (workflow.containsGenericInfo(attributeKey)
                    && !valueIsContainedInSet(attributes.get(attributeKey), workflow.getGenericInformation(attributeKey))) {
                logger.debug(workflow.getName() + " : Wrong value for " + attributeKey);
                return false;
            }
        }

        logger.debug(workflow.getName() + " : Matching");
        return true;
    }

    private static boolean valueIsContainedInSet(String value, String set) {
        // Coma separated values, case and spaces are ignored
        if (value == null || set == null)
            return false;
        String[] values = set.replaceAll(" ", "").toLowerCase().split(",");
        Arrays.sort(values);
        return !(Arrays.binarySearch(values, value.toLowerCase()) < 0);
    }


}
