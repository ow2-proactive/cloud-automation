package org.ow2.proactive.brokering.triggering;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.ow2.proactive.brokering.Reference;
import org.ow2.proactive.brokering.References;
import org.ow2.proactive.brokering.occi.Resource;
import org.ow2.proactive.brokering.occi.infrastructure.ActionTrigger;
import org.ow2.proactive.brokering.utils.HttpUtility;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActionTriggerTest {

    private static final Integer PERIODMS = 10;

    private static Integer trueActions = 0;
    private static Integer falseActions = 0;

    @Test
    public void loadBalancerStartScheduleOnce_Test() throws Exception {

        ActionTrigger actionTrigger = ActionTrigger.getInstance();

        initializeCallbackCounters();

        Assert.assertTrue(trueActions == 0);
        Assert.assertTrue(falseActions == 0);

        Map<String, String> loadBalancerAttributes =
                getCreationScheduleOnceActionTriggerAttributes();
        References references = actionTrigger.request(
                Resource.ACTION_TRIGGER_CATEGORY_NAME, "create",
                "scheduleonce", loadBalancerAttributes);

        Thread.sleep(100 * PERIODMS);

        Assert.assertTrue(trueActions == 1);
        Assert.assertTrue(falseActions == 0);

    }

    @Test
    public void loadBalancerStart_Test() throws Exception {
        // This test will post a loadbalancer rule with 3 scripts: a condition
        // script, a true action script, and a false script.
        // If condition is true, true action script will be executed. This test
        // executes a true action script that increases a trueActions counter.
        // The test is based in counting if these variables are incremented or not.

        ActionTrigger actionTrigger = ActionTrigger.getInstance();
        String uuid = UUID.randomUUID().toString();

        initializeCallbackCounters();

        Assert.assertTrue(trueActions == 0);
        Assert.assertTrue(falseActions == 0);
        Assert.assertTrue(actionTrigger.getTimers().size() == 0);

        Map<String, String> loadBalancerAttributes = getCreationActionTriggerAttributes(uuid);
        actionTrigger.request(
                Resource.ACTION_TRIGGER_CATEGORY_NAME, "create",
                "schedule", loadBalancerAttributes); // load balancer rule started

        Thread.sleep(100 * PERIODMS);
        Assert.assertTrue(trueActions > 20);
        Assert.assertTrue(falseActions > 20);
        Assert.assertTrue(actionTrigger.getTimers().size() == 1);

        loadBalancerAttributes = getDeletionActionTriggerAttributes(uuid);
        actionTrigger.request(
                Resource.ACTION_TRIGGER_CATEGORY_NAME, "delete",
                null, loadBalancerAttributes); // load balancer rule stopped

        Thread.sleep(10 * PERIODMS);
        initializeCallbackCounters();
        Thread.sleep(100 * PERIODMS);

        Assert.assertTrue(trueActions == 0);
        Assert.assertTrue(falseActions == 0);
        Assert.assertTrue(actionTrigger.getTimers().size() == 0);

    }

    @Test
    public void loadBalancerStartScheduleOnceWithNonEncodedScript_Test() throws Exception {

        ActionTrigger actionTrigger = ActionTrigger.getInstance();

        Map<String, String> loadBalancerAttributes =
                getCreationScheduleOnceActionTriggerAttributes();

        loadBalancerAttributes.put(
                ActionTrigger.OCCI_MONITORING_ACTION,
                getScriptAsString("ActionTrueScript.groovy")); // overwritten

        References references = actionTrigger.request(
                Resource.ACTION_TRIGGER_CATEGORY_NAME, "create",
                "scheduleonce", loadBalancerAttributes);

        Assert.assertTrue(references.size() == 1);

        Reference uniqueReference = references.get(0);
        Assert.assertTrue(uniqueReference.isSuccessfullySubmitted() == false);
        Assert.assertTrue(uniqueReference.getSubmissionMessage().contains("MultipleCompilationErrors"));

    }

    @Test
    public void loadBalancerStartScheduleWithNonEncodedScript_Test() throws Exception {

        String uuid = UUID.randomUUID().toString();

        ActionTrigger actionTrigger = ActionTrigger.getInstance();

        Map<String, String> atts =
                getCreationActionTriggerAttributes(uuid);

        atts.put(
                ActionTrigger.OCCI_MONITORING_TRUEACTION,
                getScriptAsString("ActionTrueScript.groovy")); // overwriten

        References references = actionTrigger.request(
                Resource.ACTION_TRIGGER_CATEGORY_NAME, "create",
                "schedule", atts);

        Assert.assertTrue(references.size() == 1);

        Reference uniqueReference = references.get(0);
        Assert.assertTrue(uniqueReference.isSuccessfullySubmitted() == false);
        Assert.assertTrue(uniqueReference.getSubmissionMessage().contains("MultipleCompilationErrors"));
    }

    @Test
    public void loadBalancerStartScheduleWithNoDelayArgument_Test() throws Exception {

        String uuid = UUID.randomUUID().toString();

        ActionTrigger actionTrigger = ActionTrigger.getInstance();

        Map<String, String> loadBalancerAttributes =
                getCreationActionTriggerAttributes(uuid);

        loadBalancerAttributes.remove(ActionTrigger.OCCI_MONITORING_PERIODMS);

        References references = actionTrigger.request(
                Resource.ACTION_TRIGGER_CATEGORY_NAME, "create",
                "schedule", loadBalancerAttributes);

        Assert.assertTrue(references.size() == 1);

        Reference uniqueReference = references.get(0);
        Assert.assertTrue(uniqueReference.isSuccessfullySubmitted() == false);
        Assert.assertTrue(uniqueReference.getSubmissionMessage().contains("delay"));
        Assert.assertTrue(uniqueReference.getSubmissionMessage().contains("null"));

    }

    private Map<String, String> getCreationScheduleOnceActionTriggerAttributes()
            throws IOException {
        Map<String, String> loadBalancerAttributes = new HashMap<String, String>();
        loadBalancerAttributes.put(
                ActionTrigger.OCCI_MONITORING_ACTION,
                getScriptAsEncodedString("ActionTrueScript.groovy"));
        loadBalancerAttributes.put(
                ActionTrigger.OCCI_CORE_ID,
                UUID.randomUUID().toString());
        return loadBalancerAttributes;
    }

    private Map<String, String> getDeletionActionTriggerAttributes(String uuid) {
        Map<String, String> loadBalancerAttributes = new HashMap<String, String>();
        loadBalancerAttributes.put(ActionTrigger.OCCI_CORE_ID, uuid);
        return loadBalancerAttributes;
    }

    private Map<String, String> getCreationActionTriggerAttributes(String uuid) throws IOException {
        Map<String, String> loadBalancerAttributes = new HashMap<String, String>();
        loadBalancerAttributes.put(ActionTrigger.OCCI_CORE_ID, uuid);
        loadBalancerAttributes.put(ActionTrigger.OCCI_CONDITION_SCRIPT,
                                   getScriptAsEncodedString("ConditionScript.groovy"));      // half times true, half times false
        loadBalancerAttributes.put(ActionTrigger.OCCI_MONITORING_FALSEACTION,
                                   getScriptAsEncodedString("ActionFalseScript.groovy"));
        loadBalancerAttributes.put(ActionTrigger.OCCI_MONITORING_TRUEACTION,
                                   getScriptAsEncodedString("ActionTrueScript.groovy"));
        loadBalancerAttributes.put(ActionTrigger.OCCI_MONITORING_PERIODMS, PERIODMS.toString());
        return loadBalancerAttributes;
    }

    private void initializeCallbackCounters() {
        trueActions = 0;
        falseActions = 0;
    }

    private String getScriptAsEncodedString(String path) throws IOException {
        return HttpUtility.encodeBase64(getScriptAsString(path));
    }

    private String getScriptAsString(String path) throws IOException {
        File f = new File(this.getClass().getResource(path).getFile());
        return FileUtils.readFileToString(f);
    }

    public static void addOneFalseActionExecuted() {
        falseActions++;
    }

    public static void addOneTrueActionExecuted() {
        trueActions++;
    }

}

