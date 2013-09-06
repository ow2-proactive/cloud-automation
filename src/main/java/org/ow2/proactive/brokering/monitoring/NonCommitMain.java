package org.ow2.proactive.brokering.monitoring;

import org.ow2.proactive.brokering.Configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class NonCommitMain {
    public static void main(String[] args) throws Exception {
        MonitoringProxy p = new MonitoringProxy.Builder()
                .setRestUrl("http://localhost:8080/rest/rest")
                .setCredentials("demo", "demo")
                .setNodeSourceName("OPENSTACK")
                .setJmxUrl("service:jmx:rmi:///jndi/rmi://localhost:5822/JMXRMAgent")
                .build();

        InfrastructureMonitoring m = new InfrastructureMonitoring(p);
        m.getHosts();
        m.getVMs();
        System.out.println(m.getHostProperties("computenode1").get("proactive.sigar.jmx.url"));

    }

    public static void initialize() throws Exception {
        try {
            File file = new File("src/main/resources/config/configuration.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Configuration rules = (Configuration) jaxbUnmarshaller.unmarshal(file);
            System.out.println(rules);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

}
