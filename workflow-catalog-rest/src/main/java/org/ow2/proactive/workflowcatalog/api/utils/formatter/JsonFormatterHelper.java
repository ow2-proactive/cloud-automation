package org.ow2.proactive.workflowcatalog.api.utils.formatter;

import com.google.gson.Gson;
import org.ow2.proactive.workflowcatalog.Workflow;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.WorkflowBean;

import java.util.ArrayList;
import java.util.Collection;

public class JsonFormatterHelper {

    private static Gson gson = new Gson();

    public static String format(Collection<Workflow> workflows) {
        Collection<WorkflowBean> collection = new ArrayList<WorkflowBean>(workflows.size());
        for (Workflow workflow: workflows) {
            collection.add(new WorkflowBean(workflow));
        }
        return gson.toJson(collection);
    }

}
