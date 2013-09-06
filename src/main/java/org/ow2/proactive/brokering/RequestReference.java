package org.ow2.proactive.brokering;

import javax.json.*;
import java.io.StringReader;

public class RequestReference {

    private boolean success;
    private int jobId;
    private String jobReadableName;

    public RequestReference(boolean success) {
        this(success, null);
    }

    public RequestReference(boolean success, String json) {
        if (json != null) {
            JsonObject ob = Json.createReader(new StringReader(json)).readObject();
            this.jobId = ob.getInt("id");
            this.jobReadableName = ob.getString("readableName");
        }
        this.success = success;
    }

    public int getId() {
        return jobId;
    }

    public String getJobReadableName() {
        return jobReadableName;
    }

    public boolean isSubmitted() {
        return success;
    }

    public String toString() {
        return "Success: " + success + ", ID: " + jobId + ", name: " + jobReadableName;
    }

}
