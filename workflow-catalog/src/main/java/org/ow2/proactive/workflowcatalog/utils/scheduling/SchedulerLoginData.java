package org.ow2.proactive.workflowcatalog.utils.scheduling;

public class SchedulerLoginData {

    public String schedulerUrl;
    public String schedulerUsername;
    public String schedulerPassword;
    public Boolean insecureMode;

    public SchedulerLoginData(
            String schedulerUrl, String schedulerUsername,
            String schedulerPassword, Boolean insecureMode) {

        this.schedulerUrl = schedulerUrl;
        this.schedulerUsername = schedulerUsername;
        this.schedulerPassword = schedulerPassword;
        this.insecureMode = insecureMode;

    }

    public SchedulerLoginData(
            String schedulerUrl, Boolean insecureMode) {

        this.schedulerUrl = schedulerUrl;
        this.insecureMode = insecureMode;

    }

}
