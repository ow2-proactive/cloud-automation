package org.ow2.proactive.brokering.updater.requests;

public class FailedRequest extends UpdaterRequest {

    private String errorMessage;

    public FailedRequest() { }

    public FailedRequest (String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
