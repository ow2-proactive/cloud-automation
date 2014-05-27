package org.ow2.proactive.workflowcatalog.utils.scheduling;

import java.util.Map;

public class TasksResults {

    private Map<String, String> results;

    public TasksResults(Map<String, String> map) {
        this.results = map;
    }

    public Map<String, String> getResults() {
        return this.results;
    }

    @Override
    public String toString() {
        return "TasksResults{" +
          "results=" + results +
          '}';
    }
}
