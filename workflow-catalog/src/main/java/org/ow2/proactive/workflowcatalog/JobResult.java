package org.ow2.proactive.workflowcatalog;

import javax.json.*;
import java.util.Map;

public class JobResult {

    public String jobId;
    public Map<String, String> taskResults;

    public JobResult() {}

    public JobResult(Reference reference, Map<String, String> taskResults) {
        this.jobId = reference.getId();
        this.taskResults = taskResults;
    }

}
