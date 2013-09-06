package org.ow2.proactive.brokering.loadbalancing;

import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.Broker;
import org.ow2.proactive.brokering.RequestReference;
import org.ow2.proactive.brokering.monitoring.NonCommitMain;

import java.util.HashMap;
import java.util.Map;

public class Script {

    private static final Logger logger = Logger.getLogger(Broker.class.getName());

    public static void main(String[] args) throws Exception {

        // TODO find out how to submit a job without changing all Brian's work
        //
        NonCommitMain.initialize();

        // Broker initialization
        ////////////////////////////////
        Broker broker = Broker.getInstance();

        Map<String, String> map = new HashMap<String, String>();
        map.put("rule","cloudwatt");
        map.put("utils.scripts.server.address","http://10.0.0.2:8000");
        // Monitoring initialization
        ////////////////////////////////
        //MonitoringProxy proxy = new MonitoringProxy.Builder()
        //        .setRestUrl("https://portal.cloud.sophia.inria.fr/tp/SchedulingRest/rest")
        //        .setCredentials("admin", "admin")
        //        .setNodeSourceName("OPENSTACK")
        //        .setJmxUrl("service:jmx:rmi:///jndi/rmi://localhost:5822/JMXRMAgent")
        //        .build();
        //InfrastructureMonitoring m = new InfrastructureMonitoring(proxy);


        // Initialization of databases
        ////////////////////////////////
        // create a central VM (vm-image=central, nodetoken=ntcentral) (new Action)
        // initialize using a sort of script=central-init.sh  through a workflow

        RequestReference result = broker.request("compute", "create", map);
        if (result.isSubmitted())
            System.out.println("Job submitted: " + result);

        while (true) {
            broker.getRequestResult(result.getId());
        }


        // check that there is a central vm started with nodetoken=ntcentral (new Condition)
        // then continue

        //System.out.println("VMs: " + m.getVMs().length);
        //System.out.println(m.getVMSuchThat("pflags.proactivenodename", "ntcentral"));
        //System.out.println(m.getVMSuchThat("pflags.proactivenodename", "realvm1"));

        // check that the services are correctly initialized (new Condition)
        // then continue

        // check that there are enough slaves (new Condition)
        // or create a slave VM (vm-image=slave, nodetoken=ntslave01, script=slave-init.sh) (new Action)

        // check that there are not too many slaves (new Condition)
        // or remove a slave VM (nodetoken=ntslave<criteria>, script=slave-stop-and-poweroff.sh) (new Action)


    }

}
