/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 2013-2015 ActiveEon
 * 
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * $$ACTIVEEON_INITIAL_DEV$$
 */


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

    public List<UnknownRequest> getFailedRequests() {
        return filter(new UnknownRequest());
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

    @SuppressWarnings("unchecked")
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
            String message = "Error parsing json (" + e.getMessage() + ") for task '" +
                    taskName + "' result '" + taskResult + "'";
            logger.warn(message);
            requests.add( new UnknownRequest(message + "\n"));
        }

    }

    private UpdaterRequest processValue(String key, JsonObject taskResultJson) {
        if (key.equals(UpdaterRequest.CREATE_KEY)) {
            JsonObject createData = taskResultJson.getJsonObject(key);
            return new CreateInstanceRequest(createData);
        } else if (key.equals(UpdaterRequest.UPDATE_KEY)) {
            JsonObject updateData = taskResultJson.getJsonObject(key);
            return new UpdateInstanceRequest(updateData);
        } else {
            return new UpdateAttributeRequest(key, Utils.getString(taskResultJson, key));
        }
    }

    private String toString(List<UnknownRequest> requests) {
        StringBuilder s = new StringBuilder();
        for (UnknownRequest f: requests) {
            s.append(f.getErrorMessage());
            s.append("\n\n\n");
        }
        return Utils.escapeAttribute(s.toString());
    }

}
