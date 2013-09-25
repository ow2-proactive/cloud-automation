package org.ow2.proactive.brokering.monitoring;

import org.ow2.proactive.brokering.utils.scheduling.SchedulerLoginData;

public class SingletonMonitoring {

    private static SchedulerLoginData schedulerLoginData;

    public static void configure(SchedulerLoginData schedulerLoginData) {
        SingletonMonitoring.schedulerLoginData = schedulerLoginData;
    }

    public static InfrastructureMonitoring getInfrastructureMonitoring(String jmxUrl, String nodeSource) {
        MonitoringProxy proxy = new MonitoringProxy.Builder()
                .setRestUrl(schedulerLoginData.schedulerUrl)
                .setCredentials(schedulerLoginData.schedulerUsername, schedulerLoginData.schedulerPassword)
                .setNodeSourceName(nodeSource)
                .setJmxUrl(jmxUrl)
                .setInsecureAccess()
                .build();
        return new InfrastructureMonitoring(proxy);
    }

}
