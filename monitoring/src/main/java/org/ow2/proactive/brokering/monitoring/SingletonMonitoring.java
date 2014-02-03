package org.ow2.proactive.brokering.monitoring;

import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerLoginData;

import java.lang.RuntimeException;

public class SingletonMonitoring {

    private static SchedulerLoginData schedulerLoginData;

    public static void configure(SchedulerLoginData schedulerLoginData) {
        SingletonMonitoring.schedulerLoginData = schedulerLoginData;
    }

    public static InfrastructureMonitoring getInfrastructureMonitoring(String jmxUrl, String nodeSource) {

        if (schedulerLoginData == null)
            throw new RuntimeException("Not configured");

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
