package org.ow2.proactive.workflowcatalog.utils.scheduling;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonParsingException;
import java.io.StringReader;

public class JobSubmissionResponse {

    private String message;
    private String id;
    private Boolean valid;

    public JobSubmissionResponse(String json) {
        JsonObject ob = null;

        try {
            ob = Json.createReader(new StringReader(json)).readObject();
        } catch (JsonParsingException e) {
            throw new RuntimeException("Cannot parse response: " + json, e);
        }

        if (jobSubmittedCorrectly(ob)) {
            this.id = ob.getInt("id") + "";
            this.message = ob.getString("readableName");
            this.valid = true;
        } else {
            this.id = null;
            this.message = extendErrorInformation(ob);
            this.valid = false;
        }
    }

    private String extendErrorInformation(JsonObject object) {
        String errorMessage = object.getString("errorMessage");
        StringBuilder builder = new StringBuilder();
        if (errorMessage.contains("is missing")){
            builder.append("A mandatory variable is missing: ");
        }
        builder.append(errorMessage);
        return builder.toString();
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public Boolean getValid() {
        return valid;
    }

    private static Boolean jobSubmittedCorrectly(JsonObject jsonObject) {
        try {
            jsonObject.getInt("id");
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }
}
