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
    public void loadBalancerStartScheduleOnce_EncodedScripts_Test() throws Exception {

        Map<String, String> loadBalancerAttributes =
                getCreationScheduleOnceActionTriggerAttributes();

        loadBalancerStartScheduleOnce(loadBalancerAttributes);

    }

    @Test
    public void loadBalancerStartScheduleOnce_ClassnameScripts_Test() throws Exception {

        Map<String, String> loadBalancerAttributes =
                getCreationScheduleOnceActionTriggerAttributes();

        loadBalancerAttributes.remove(ActionTrigger.OCCI_MONITORING_ACTION);
        loadBalancerAttributes.put(ActionTrigger.OCCI_MONITORING_ACTION_CLASSNAME, ActionTrueScript.class.getName());

        loadBalancerStartScheduleOnce(loadBalancerAttributes);

    }

    private void loadBalancerStartScheduleOnce(Map<String, String> atts) throws Exception {

        ActionTrigger actionTrigger = ActionTrigger.getInstance();

        initializeCallbackCounters();

        Assert.assertTrue(trueActions == 0);
        Assert.assertTrue(falseActions == 0);

        References references = actionTrigger.request(
                Resource.ACTION_TRIGGER_CATEGORY_NAME, "create",
                "scheduleonce", atts);

        Thread.sleep(100 * PERIODMS);

        Assert.assertTrue(trueActions == 1);
        Assert.assertTrue(falseActions == 0);

    }

    @Test
    public void loadBalancerStart_EncodedScripts_Test() throws Exception {
        String uuid = UUID.randomUUID().toString();
        Map<String, String> loadBalancerAttributes = getCreationActionTriggerAttributes(uuid);
        loadBalancerStart_Test(loadBalancerAttributes);
    }

    @Test
    public void loadBalancerStart_ClassnameScripts_Test() throws Exception {
        String uuid = UUID.randomUUID().toString();
        Map<String, String> atts = getCreationActionTriggerAttributes(uuid);

        atts.remove(ActionTrigger.OCCI_CONDITION_SCRIPT);
        atts.remove(ActionTrigger.OCCI_MONITORING_FALSEACTION);
        atts.remove(ActionTrigger.OCCI_MONITORING_TRUEACTION);

        atts.put(ActionTrigger.OCCI_CONDITION_SCRIPT_CLASSNAME, ConditionScript.class.getName());
        atts.put(ActionTrigger.OCCI_MONITORING_FALSEACTION_CLASSNAME, ActionFalseScript.class.getName());
        atts.put(ActionTrigger.OCCI_MONITORING_TRUEACTION_CLASSNAME, ActionTrueScript.class.getName());

        loadBalancerStart_Test(atts);
    }


    private void loadBalancerStart_Test(Map<String, String> atts) throws Exception {

        // This test will post a loadbalancer rule with 3 scripts: a condition
        // script, a true action script, and a false script.
        // If condition is true, true action script will be executed. This test
        // executes a true action script that increases a trueActions counter.
        // The test is based in counting if these variables are incremented or not.
        ActionTrigger actionTrigger = ActionTrigger.getInstance();

        initializeCallbackCounters();

        Assert.assertTrue(trueActions == 0);
        Assert.assertTrue(falseActions == 0);
        Assert.assertTrue(actionTrigger.getTimers().size() == 0);

        actionTrigger.request(
                Resource.ACTION_TRIGGER_CATEGORY_NAME, "create",
                "schedule", atts); // load balancer rule started

        Thread.sleep(100 * PERIODMS);
        Assert.assertTrue(trueActions > 20);
        Assert.assertTrue(falseActions > 20);
        Assert.assertTrue(actionTrigger.getTimers().size() == 1);

        atts = getDeletionActionTriggerAttributes(getUuid(atts));
        actionTrigger.request(
                Resource.ACTION_TRIGGER_CATEGORY_NAME, "delete",
                null, atts); // load balancer rule stopped

        Thread.sleep(10 * PERIODMS);
        initializeCallbackCounters();
        Thread.sleep(100 * PERIODMS);

        Assert.assertTrue(trueActions == 0);
        Assert.assertTrue(falseActions == 0);
        Assert.assertTrue(actionTrigger.getTimers().size() == 0);

    }

    private String getUuid(Map<String, String> atts) {
        return atts.get(ActionTrigger.OCCI_CORE_ID);
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

