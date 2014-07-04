package org.ow2.proactive.workflowcatalog.utils.scheduling;

public class SchedulerLoginData {

    public String schedulerUrl;
    public String schedulerUsername;
    public String schedulerPassword;
    public String schedulerCredentials;
    public Boolean insecureMode;

    public SchedulerLoginData(
            String schedulerUrl, String schedulerUsername,
            String schedulerPassword, Boolean insecureMode) {
        this(schedulerUrl, schedulerUsername, schedulerPassword, null, insecureMode);
    }

    public SchedulerLoginData(
            String schedulerUrl, Boolean insecureMode) {
        this(schedulerUrl, null, null, null, insecureMode);
    }

    public SchedulerLoginData(
            String schedulerUrl, String schedulerUsername, String schedulerPassword, String schedulerCredentials, Boolean insecureMode) {
        this.schedulerUrl = schedulerUrl;
        this.schedulerUsername = schedulerUsername;
        this.schedulerPassword = schedulerPassword;
        this.schedulerCredentials = schedulerCredentials;
        this.insecureMode = insecureMode;
    }

}
