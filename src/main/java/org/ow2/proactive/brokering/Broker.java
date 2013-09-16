package org.ow2.proactive.brokering;

import groovy.lang.GroovyClassLoader;
import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.monitoring.SingletonMonitoring;
import org.ow2.proactive.brokering.occi.infrastructure.ActionTrigger;
import org.ow2.proactive.brokering.occi.Resource;
import org.ow2.proactive.brokering.utils.scheduling.SchedulerLoginData;
import org.ow2.proactive.brokering.utils.scheduling.SchedulerProxy;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class Broker {

    private static final Logger logger = Logger.getLogger(Broker.class.getName());

    private static Broker instance;

    private SchedulerLoginData loginData;
    private SchedulerProxy scheduler;
    private Catalog catalog;
    private Rules rules;

    private Broker() {
        try {
            File configFile = new File(Broker.class.getResource("/config/configuration.xml").getFile());
            JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Configuration config = (Configuration) jaxbUnmarshaller.unmarshal(configFile);

            loginData = new SchedulerLoginData(
                    config.scheduler.url, config.scheduler.username, config.scheduler.password );
            SchedulerProxy.initializeSchedulerUtility(config.security.insecuremode);
            scheduler = new SchedulerProxy(loginData);
            SingletonMonitoring.configure(loginData);


            File catalogPath = new File(config.catalog.path);
            if (!catalogPath.isDirectory()) {
                catalogPath = new File(Broker.class.getResource("/config/catalog").getFile());
            }

            File rulesPath = new File(config.rules.path);
            if (!rulesPath.isDirectory()) {
                rulesPath = new File(Broker.class.getResource("/config/rules").getFile());
            }

            catalog = new Catalog(catalogPath, config.catalog.refresh * 1000);
            rules = new Rules(rulesPath, config.rules.refresh * 1000);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public static Broker getInstance() {
        // TODO : Double check locking
        if (instance == null) {
            instance = new Broker();
        }
        return instance;
    }

    public References request(String category, String operation, Map<String, String> attributes) throws Exception {
        return request(category, operation, null, attributes);
    }

    public References request(String category, String operation, String action, Map<String, String> attributes) throws Exception {
        logger.debug("Request       : category=" + category + ", operation=" + operation + " action=" + action);
        logger.debug("   attributes : " + showSorted(attributes));

        References references = new References();
        references.addAll(processNonWorkflowRequest(category, operation, action, attributes));

        for (Workflow workflow : catalog.getWorkflows()) {
            if (workflow.isCompliant(category, operation, action, attributes)) {
                int appliedRules = this.applyRules(attributes, rules);
//              if (workflow.isDeepCompliant(attributes))
                File jobFile = workflow.configure(attributes);
                logger.debug("Generated job file : " + jobFile.getAbsolutePath());
                String output = scheduler.submitJob(jobFile);
                Reference ref = Reference.buildJobReference(true, output);
                references.add(ref);
                if (ref.isSuccessfullySubmitted()){
                    logger.info("Workflow '" + workflow + "' configured (" + appliedRules + " rules applied) and submitted (Job ID=" + ref.getId() + ")");
                }
            }
        }
        logger.info("No matching workflow");
        return references;
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    //
    //                  PRIVATE METHODS
    //
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Apply proper rules
     *
     * @param attributes
     * @param rules
     * @return the number of rules applied
     */
    private int applyRules(Map<String, String> attributes, Rules rules) {
        int count = 0;

        List<File> ruleFiles = rules.getRules();
        GroovyClassLoader gcl = new GroovyClassLoader();
        for (File file : ruleFiles) {
            try {
                Class clazz = gcl.parseClass(file);
                Rule rule = (Rule) clazz.newInstance();
                if (rule.match(attributes)) {
                    rule.apply(attributes);
                    count++;
                }
            } catch (Throwable e) {
                e.printStackTrace();
                logger.debug("Error when loading a rules file : " + file.getName(), e);
            }
        }
        return count;
    }

    private String showSorted(Map<String, String> map) {
        return new TreeMap<String, String>(map).toString();
    }

    private References processNonWorkflowRequest(String category, String operation, String action, Map<String, String> attributes) throws Exception {
        if (Resource.ACTION_TRIGGER_CATEGORY_NAME.equalsIgnoreCase(category)) {
            return ActionTrigger.getInstance().request(category, operation, action, attributes);
        }
        return new References();
    }

}
