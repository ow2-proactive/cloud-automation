package org.ow2.proactive.brokering;

import groovy.lang.GroovyClassLoader;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.occi.categories.Categories;
import org.ow2.proactive.brokering.occi.client.ActionTriggerHandler;
import org.ow2.proactive.brokering.updater.Updater;
import org.ow2.proactive.workflowcatalog.Catalog;
import org.ow2.proactive.workflowcatalog.Reference;
import org.ow2.proactive.workflowcatalog.References;
import org.ow2.proactive.workflowcatalog.Workflow;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerProxy;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class Broker {

    private static final Logger logger = Logger.getLogger(Broker.class.getName());

    private static Broker instance = new Broker();

    private SchedulerProxy scheduler;
    private Updater updater;
    private Catalog catalog;
    private Rules rules;

    private Broker() { }

    public void initialize(Configuration config, Updater updater, SchedulerProxy sched) {
        File catalogPath = getPath(config.catalog.path, "/config/catalog");
        File rulesPath = getPath(config.rules.path, "/config/rules");

        this.scheduler = sched;
        this.catalog = new Catalog(catalogPath, config.catalog.refresh * 1000, new CatalogToResource());
        this.rules = new Rules(rulesPath, config.rules.refresh * 1000);
        this.updater = updater;
    }

    public static Broker getInstance() {
        return instance;
    }

    public References request(String category, String operation, Map<String, String> attributes) throws Exception {
        return request(category, operation, null, attributes);
    }

    public References request(String category, String operation, String action, Map<String, String> attributes) throws Exception {
        logger.debug("Request       : category=" + category + ", operation=" + operation + " action=" + action);
        logger.debug("   attributes : " + showSorted(attributes));

        References refNonWorkflow = processNonWorkflowRequest(category, operation, action, attributes);
        References refWorkflow = processWorkflowRequest(category, operation, action, attributes);

        References references = new References();
        references.addAll(refNonWorkflow);
        references.addAll(refWorkflow);

        for (Reference ref : refWorkflow) {
            updater.addResourceToTheUpdateQueue(ref, getUuid(attributes));
        }

        return references;
    }


    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    //
    //                  PRIVATE METHODS
    //
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    private File getPath(String path, String defaultPath) {
        File c = new File(path);
        if (!c.isDirectory()) {
            c = new File(Broker.class.getResource(defaultPath).getFile());
        }
        return c;
    }

    private String getUuid(Map<String, String> attributes) {
        return attributes.get("occi.core.id");
    }

    private References processWorkflowRequest(
            String category,
            String operation,
            String action,
            Map<String, String> attributes)
            throws Exception {

        References references = new References();
        for (Workflow workflow : catalog.getWorkflows()) {
            if (OcciWorkflowUtils.isCompliant(workflow, category, operation, action, attributes)) {
                int appliedRules = this.applyRules(attributes, rules);
                //              if (workflow.isDeepCompliant(attributes))
                File jobFile = workflow.configure(attributes);

                logger.info("Generated job file : " + jobFile.getAbsolutePath());
                logger.debug(FileUtils.readFileToString(jobFile));

                JobIdData response = scheduler.submitJob(jobFile);
                Reference ref = Reference.buildJobReference(workflow.getName(), response);
                references.add(ref);
                logger.info(
                        String.format("Workflow '%s' configured ('%d' rules applied) and submitted (Job ID='%s')",
                                      workflow.getName(), appliedRules, ref.getId()));
            }
        }

        return references;
    }

    private References processNonWorkflowRequest(
            String category,
            String operation,
            String action,
            Map<String, String> attributes) {

        if (Categories.ACTION_TRIGGER.equals(Categories.fromString(category))) {
            int appliedRules = this.applyRules(attributes, rules);
            References references = ActionTriggerHandler.getInstance().request(operation, action, attributes);
            logger.info("Action trigger configured: (" + appliedRules + " rules applied)");
            return references;
        }
        return new References();
    }

    /**
     * Apply proper rules
     *
     * @param attributes
     * @param rules
     * @return the number of rules applied
     */
    private int applyRules(Map<String, String> attributes, Rules rules) {
        int count = 0;

        List<File> ruleFiles = rules.getScripts();
        GroovyClassLoader gcl = new GroovyClassLoader();
        for (File file : ruleFiles) {
            try {
                logger.debug("Rule: " + file.getName());
                Class clazz = gcl.parseClass(file);
                Rule rule = (Rule) clazz.newInstance();
                if (rule.match(attributes)) {
                    rule.apply(attributes);
                    count++;
                    logger.debug("Applying rule: " + rule.getClass().getName());
                }
            } catch (Throwable e) {
                logger.warn("Error loading rule : " + file.getName(), e);
            }
        }
        return count;
    }

    private String showSorted(Map<String, String> map) {
        return new TreeMap<String, String>(map).toString();
    }

}
