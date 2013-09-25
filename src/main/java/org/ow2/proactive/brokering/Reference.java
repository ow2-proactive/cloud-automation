package org.ow2.proactive.brokering;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.StringReader;

public class Reference {

    private SubmissionStatus successfullySubmitted;
    private String submissionId;
    private String submissionMessage;
    private Nature natureOfReference;

    private Reference(Nature natureOfReference, SubmissionStatus submitted, String message, String id) {
        this.natureOfReference = natureOfReference;
        this.successfullySubmitted = submitted;
        this.submissionMessage = message;
        this.submissionId = id;
    }

    public static Reference buildActionTriggerReference(String message, String id) {
        return new Reference(Nature.NATURE_TRIGGER, SubmissionStatus.SUBMISSION_DONE, message, id);
    }

    public static Reference buildJobReference(String json) {
        JsonObject ob = Json.createReader(new StringReader(json)).readObject();
        Boolean isValid = jobSubmittedCorrectly(ob);
        if (isValid) {
            String id = ob.getInt("id") + "";
            String message = ob.getString("readableName");
            return new Reference(Nature.NATURE_JOB, SubmissionStatus.SUBMISSION_DONE, message, id);
        } else {
            String errorMessage = ob.getString("errorMessage");
            return new Reference(Nature.NATURE_JOB, SubmissionStatus.SUBMISSION_NOT_DONE, errorMessage, null);
        }
    }

    private static Boolean jobSubmittedCorrectly(JsonObject jsonObject) {
        try {
            jsonObject.getInt("id");
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public String getId() {
        return submissionId;
    }

    public Boolean isSuccessfullySubmitted() {
        return successfullySubmitted == SubmissionStatus.SUBMISSION_DONE;
    }

    public String toString() {
        return
                "nature: " + natureOfReference +
                        ", submitted: " + successfullySubmitted +
                        ", id: " + submissionId +
                        ", submissionMessage: " + submissionMessage;
    }

    static enum Nature {

        NATURE_UNKNOWN(0), NATURE_JOB(1), NATURE_TRIGGER(2);

        private int nature;

        private Nature(int nature) {
            this.nature = nature;
        }

    }

    static enum SubmissionStatus {

        SUBMISSION_UNKNOWN(0), SUBMISSION_DONE(1), SUBMISSION_NOT_DONE(2);

        private int submissionStatus;

        private SubmissionStatus(int submissionStatus) {
            this.submissionStatus = submissionStatus;
        }

    }
}
