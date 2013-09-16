package org.ow2.proactive.brokering.triggering;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.ow2.proactive.brokering.occi.Resource;
import org.ow2.proactive.brokering.occi.infrastructure.ActionTrigger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActionTriggerTest {

    private static final String OCCI_CORE_ID = "occi.core.id";
    private static final Integer PERIODMS = 10;

    private static Integer trueActions = 0;
    private static Integer falseActions = 0;

    @Test
    public void loadBalancerStartScheduleOnce_Test() throws Exception {

        ActionTrigger actionTrigger = ActionTrigger.getInstance();

        initializeCallbackCounters();

        Assert.assertTrue(trueActions == 0);
        Assert.assertTrue(falseActions == 0);

        Map<String, String> loadBalancerAttributes = getCreationScheduleOnceActionTriggerAttributes();
        actionTrigger.request(Resource.ACTION_TRIGGER_CATEGORY_NAME, "create", "scheduleonce", loadBalancerAttributes);

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
        String uuid = generateUUID();

        initializeCallbackCounters();

        Assert.assertTrue(trueActions == 0);
        Assert.assertTrue(falseActions == 0);
        Assert.assertTrue(actionTrigger.getTimers().size() == 0);

        Map<String, String> loadBalancerAttributes = getCreationActionTriggerAttributes(uuid);
        actionTrigger.request(Resource.ACTION_TRIGGER_CATEGORY_NAME, "create", "schedule", loadBalancerAttributes); // load balancer rule started

        Thread.sleep(100 * PERIODMS);
        Assert.assertTrue(trueActions > 20);
        Assert.assertTrue(falseActions > 20);
        Assert.assertTrue(actionTrigger.getTimers().size() == 1);

        loadBalancerAttributes = getDeletionActionTriggerAttributes(uuid);
        actionTrigger.request(Resource.ACTION_TRIGGER_CATEGORY_NAME, "delete", null, loadBalancerAttributes); // load balancer rule stopped

        Thread.sleep(10 * PERIODMS);
        initializeCallbackCounters();
        Thread.sleep(100 * PERIODMS);

        Assert.assertTrue(trueActions == 0);
        Assert.assertTrue(falseActions == 0);
        Assert.assertTrue(actionTrigger.getTimers().size() == 0);

    }

    private String generateUUID() {
        return UUID.randomUUID().toString();
    }

    private Map<String, String> getCreationScheduleOnceActionTriggerAttributes() throws IOException {
        Map<String, String> loadBalancerAttributes = new HashMap<String, String>();
        loadBalancerAttributes.put(ActionTrigger.OCCI_MONITORING_ACTION, getScriptAsString("ActionTrueScript.groovy"));
        return loadBalancerAttributes;
    }

    private Map<String, String> getDeletionActionTriggerAttributes(String uuid) {
        Map<String, String> loadBalancerAttributes = new HashMap<String, String>();
        loadBalancerAttributes.put(OCCI_CORE_ID, uuid);
        return loadBalancerAttributes;
    }

    private Map<String, String> getCreationActionTriggerAttributes(String uuid) throws IOException {
        Map<String, String> loadBalancerAttributes = new HashMap<String, String>();
        loadBalancerAttributes.put(OCCI_CORE_ID, uuid);
        loadBalancerAttributes.put(ActionTrigger.OCCI_CONDITION_SCRIPT, getScriptAsString("ConditionScript.groovy"));      // half times true, half times false
        loadBalancerAttributes.put(ActionTrigger.OCCI_MONITORING_FALSEACTION, getScriptAsString("ActionFalseScript.groovy"));
        loadBalancerAttributes.put(ActionTrigger.OCCI_MONITORING_TRUEACTION, getScriptAsString("ActionTrueScript.groovy"));
        loadBalancerAttributes.put(ActionTrigger.OCCI_MONITORING_PERIODMS, PERIODMS.toString());
        return loadBalancerAttributes;
    }

    private void initializeCallbackCounters() {
        trueActions = 0;
        falseActions = 0;
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

