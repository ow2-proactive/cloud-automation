package org.ow2.proactive.brokering;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ow2.proactive.brokering.occi.categories.Categories;
import org.ow2.proactive.brokering.occi.client.ActionTriggerHandler;
import org.ow2.proactive.workflowcatalog.Catalog;
import org.ow2.proactive.workflowcatalog.Reference;
import org.ow2.proactive.workflowcatalog.References;
import org.ow2.proactive.workflowcatalog.Workflow;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import com.google.inject.Inject;
import groovy.lang.GroovyClassLoader;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;


public class Broker {

    private static final Logger logger = Logger.getLogger(Broker.class.getName());

    private Catalog catalog;
    private Rules rules;
    private SchedulerFactory userSchedulerProxy;
    private ActionTriggerHandler actionTriggerHandler;

    @Inject
    public Broker(Catalog catalog, Rules rules, SchedulerFactory userSchedulerProxy,
      ActionTriggerHandler actionTriggerHandler) {
        this.catalog = catalog;
        this.rules = rules;
        this.userSchedulerProxy = userSchedulerProxy;
        this.actionTriggerHandler = actionTriggerHandler;
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

        return references;
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

                JobIdData response = userSchedulerProxy.getScheduler().submitJob(jobFile);
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
            References references = actionTriggerHandler.request(operation, action, attributes);
            logger.info("Action trigger configured: (" + appliedRules + " rules applied)");
            return references;
        }
        return new References();
    }

    /**
     * Apply proper rules
     *
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

    public List<String> listPossibleActions(String category, Map<String, String> attributes) {
        List<String> possibleActions = new ArrayList<String>();
        Map<String, String> copy = new HashMap<String, String>(attributes);
        // FIXME find out resource state with a better way
        copy.put("action.from-states", copy.get("occi."+categoryToAttributeName(category)+".state"));
        for (Workflow workflow : catalog.getWorkflows()) {
            if (OcciWorkflowUtils.isCompliant(workflow, category, null, null, copy)) {
                possibleActions.add(workflow.getGenericInformation("action"));
            }
        }
        return possibleActions;
    }

    private String categoryToAttributeName(String category) {
        if ("platform".equals(category)) {
            return "paas";
        } else {
            return category;
        }
    }
}
