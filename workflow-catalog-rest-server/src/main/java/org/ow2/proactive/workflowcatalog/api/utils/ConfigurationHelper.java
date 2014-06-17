package org.ow2.proactive.workflowcatalog.api.utils;

import org.ow2.proactive.workflowcatalog.api.Configuration;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerLoginData;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.InputStream;

public class ConfigurationHelper {

    public static Configuration getConfiguration() {
        try {
            String configPath = "/config/configuration.xml";
            InputStream is = ConfigurationHelper.class.getResourceAsStream(configPath);
            JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (Configuration) jaxbUnmarshaller.unmarshal(is);
        } catch (JAXBException e) {
            throw new RuntimeException("Cannot read configuration file", e);
        }
    }

    public static SchedulerLoginData getSchedulerLoginData(Configuration config) {
        return new SchedulerLoginData(
                config.scheduler.url, config.security.insecuremode);
    }

    public static File getCatalogPath(Configuration config) {
        File catalogPath = new File(config.catalog.path);
        if (!catalogPath.isDirectory())
            catalogPath = new File(ConfigurationHelper.class.getResource("/catalog").getFile());
        return catalogPath;
    }


}
