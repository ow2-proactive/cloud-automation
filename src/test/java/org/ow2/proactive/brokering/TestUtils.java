package org.ow2.proactive.brokering;

public class TestUtils {

    public static String createSubmitResponse(int jobId, String readableName) {
        return "{\"id\":" + jobId + ",\"readableName\":\"" + readableName + "\"}";
    }
}
