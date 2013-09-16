package org.ow2.proactive.brokering;

import javax.json.*;
import java.io.StringReader;

public class Reference {

    public static final int NATURE_UNKNOWN = 0;
    public static final int NATURE_JOB = 1;
    public static final int NATURE_TRIGGER = 2;

    private boolean successfullySubmitted;
    private String submissionId;
    private String submissionMessage;

    private int nature;

    // Public builders

    public static Reference buildJobReference(boolean submitted, String json) {
        JsonObject ob = Json.createReader(new StringReader(json)).readObject();
        boolean isValid = jobSubmittedCorrectly(ob);
        if (isValid) {
            String id = ob.getInt("id") + "";
            String message = ob.getString("readableName");
            return new Reference(NATURE_JOB, submitted, message, id);
        } else {
            String errorMessage = ob.getString("errorMessage");
            return new Reference(NATURE_JOB, false, errorMessage, null);
        }
    }

    public static Reference buildActionTriggerReference(boolean submitted, String message, String id) {
        return new Reference(Reference.NATURE_TRIGGER, submitted, message, id);
    }

    // Private constructor

    private Reference(int nature, boolean submitted, String message, String id) {
        this.nature = nature;
        this.successfullySubmitted = submitted;
        this.submissionMessage = message;
        this.submissionId = id;
    }

    // Getters and setters

    public boolean natureIs(int nature) {
        return (this.nature == nature);
    }

    public String getId() {
        return submissionId;
    }

    public boolean isSuccessfullySubmitted() {
        return successfullySubmitted;
    }

    public String toString() {
        return "Submitted: " + successfullySubmitted + ", id: " + submissionId + ", submissionMessage: " + submissionMessage;
    }

    // Private methods

    private static boolean jobSubmittedCorrectly(JsonObject jsonObject) {
        try {
            jsonObject.getInt("id");
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

}
