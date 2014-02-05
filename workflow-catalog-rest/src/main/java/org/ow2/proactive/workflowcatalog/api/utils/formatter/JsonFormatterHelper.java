package org.ow2.proactive.workflowcatalog.api.utils.formatter;

import com.google.gson.Gson;
import org.ow2.proactive.workflowcatalog.References;
import org.ow2.proactive.workflowcatalog.Workflow;
import org.ow2.proactive.workflowcatalog.WorkflowParameters;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.WorkflowBeanOutput;

import java.util.ArrayList;
import java.util.Collection;

public class JsonFormatterHelper {

    private static Gson gson = new Gson();

    public static String format(Collection<Workflow> workflows) {
        Collection<WorkflowBeanOutput> collection = new ArrayList<WorkflowBeanOutput>(workflows.size());
        for (Workflow workflow: workflows) {
            collection.add(new WorkflowBeanOutput(workflow));
        }
        return gson.toJson(collection);
    }

    public static String format(References references) {
        return gson.toJson(references);
    }

    public static String format(WorkflowParameters parameters) {
        return gson.toJson(parameters);
    }

    public static WorkflowParameters toWorkflowParameters(String json) {
        return gson.fromJson(json, WorkflowParameters.class);
    }

    public static String format(Throwable throwable) {
        return gson.toJson(throwable);
    }

}
