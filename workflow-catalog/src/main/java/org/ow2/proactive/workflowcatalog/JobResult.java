package org.ow2.proactive.workflowcatalog;

import java.util.Map;
import java.util.HashMap;

public class JobResult {

    public String jobId;
    public Map<String, String> taskResults;

    public JobResult() {}

    public JobResult(String jobId, Map<String, String> taskResults) {
        this.jobId = jobId;
        this.taskResults = new HashMap<String, String>();
        this.taskResults.putAll(taskResults);
    }

}
