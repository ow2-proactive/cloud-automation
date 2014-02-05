package org.ow2.proactive.brokering;

import org.apache.log4j.Logger;
import org.ow2.proactive.workflowcatalog.Workflow;

import java.io.File;
import java.util.*;

public class OcciWorkflowUtils {

    protected static final Logger logger = Logger.getLogger(OcciWorkflowUtils.class.getName());

    /**
     * Only checks compliance with the generic informations.
     *
     * @param category
     * @param operation
     * @param attributes
     * @return
     */
    public static boolean isCompliant(Workflow workflow, String category, String operation, String action, Map<String, String> attributes) {
        // Generic Informations 'category' and 'operation' must contains given 'category' and 'operation' values
        if (!valueIsContainedInSet(category, workflow.getGenericInformation("category")) ||
                !valueIsContainedInSet(operation, workflow.getGenericInformation("operation"))) {
            //logger.debug(job.getName() + " : Wrong category or operation");
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
        return true;
    }

    private static boolean valueIsContainedInSet(String value, String set) {
        // Coma separated values, case and spaces are ignored
        if (value == null || set == null)
            return false;
        String[] values = set.replaceAll(" ", "").toLowerCase().split(",");
        Arrays.sort(values);
        return !(value == null || Arrays.binarySearch(values, value.toLowerCase()) < 0);
    }


}
