package org.ow2.proactive.brokering.updater;

import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.occi.categories.Utils;
import org.ow2.proactive.brokering.updater.requests.*;
import org.ow2.proactive.workflowcatalog.utils.scheduling.TasksResults;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OcciTasksResults {

    private static final Logger logger = Logger.getLogger(OcciTasksResults.class.getName());

    private TasksResults tasksResults;
    private List<UpdaterRequest> requests;

    public OcciTasksResults(TasksResults tasksResults) {
        this.tasksResults = tasksResults;
        this.requests = processTasksResults();
    }

    public List<CreateInstanceRequest> getCreateInstanceRequests() {
        return filter(new CreateInstanceRequest());
    }

    public List<UpdateInstanceRequest> getUpdateInstanceRequests() {
        return filter(new UpdateInstanceRequest());
    }

    public List<UpdateAttributeRequest> getUpdateAttributeRequests() {
        List<UpdateAttributeRequest> attributes = filter(new UpdateAttributeRequest());
        attributes.add(new UpdateAttributeRequest("occi.error.description", toString(getFailedRequests())));
        return attributes;
    }

    public List<FailedRequest> getFailedRequests() {
        return filter(new FailedRequest());
    }

    public void add(UpdaterRequest request) {
        requests.add(request);
    }

    public Map<String, String> getUpdateAttributes() {
        Map<String, String> attributes = new HashMap<String, String>();
        List<UpdateAttributeRequest> requests = getUpdateAttributeRequests();
        for (UpdateAttributeRequest r: requests) {
            attributes.put(r.getKey(), r.getValue());
        }
        return attributes;
    }

    private <T> List<T> filter(T obj) {
        List<T> list = new ArrayList<T>();
        for (UpdaterRequest r: requests)
            if (r.getClass().isInstance(obj))
                list.add((T) r);

        return list;
    }

    private List<UpdaterRequest> processTasksResults() {

        List<UpdaterRequest> requests = new ArrayList<UpdaterRequest>();

        for (Map.Entry<String, String> taskEntry : tasksResults.getResults().entrySet())
            processTaskResult(taskEntry, requests);

        return requests;

    }

    private void processTaskResult(
            Map.Entry<String, String> taskEntry,
            List<UpdaterRequest> requests) {

        String taskName = taskEntry.getKey();
        String taskResult = taskEntry.getValue();

        try {

            JsonObject taskResultJson = Utils.convertToJson(taskResult);

            for (String key: taskResultJson.keySet())
                requests.add(processValue(key, taskResultJson));

        } catch (Exception e) {
            logger.warn("Could not interpret: " + taskResult, e);
            requests.add(new FailedRequest(taskName + ": '" + taskResult + "'\n"));
        }

    }

    private UpdaterRequest processValue(String key, JsonObject taskResultJson) {
        if (key.equals(UpdaterRequest.CREATE_KEY)) {
            JsonObject createData = taskResultJson.getJsonObject(key);
            return new CreateInstanceRequest(createData);
        } else if (key.equals(UpdaterRequest.UPDATE_KEY)) {
            throw new RuntimeException("Not implemented yet");
        } else {
            return new UpdateAttributeRequest(key, taskResultJson.getString(key));
        }
    }

    private String toString(List<FailedRequest> requests) {
        StringBuilder s = new StringBuilder();
        for (FailedRequest f: requests) {
            s.append(f.getErrorMessage());
            s.append("\n\n\n");
        }
        return Utils.escapeAttribute(s.toString());
    }

}
