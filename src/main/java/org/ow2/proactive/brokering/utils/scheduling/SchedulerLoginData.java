package org.ow2.proactive.brokering.utils.scheduling;

public class SchedulerLoginData {

    private String schedulerUrl;
    private String schedulerUsername;
    private String schedulerPassword;

    public SchedulerLoginData(String schedulerUrl, String schedulerUsername, String schedulerPassword) {
        this.schedulerUrl = schedulerUrl;
        this.schedulerUsername = schedulerUsername;
        this.schedulerPassword = schedulerPassword;
    }

    public String getSchedulerPassword() {
        return schedulerPassword;
    }

    public void setSchedulerPassword(String schedulerPassword) {
        this.schedulerPassword = schedulerPassword;
    }

    public String getSchedulerUrl() {
        return schedulerUrl;
    }

    public void setSchedulerUrl(String schedulerUrl) {
        this.schedulerUrl = schedulerUrl;
    }

    public String getSchedulerUsername() {
        return schedulerUsername;
    }

    public void setSchedulerUsername(String schedulerUsername) {
        this.schedulerUsername = schedulerUsername;
    }
}
