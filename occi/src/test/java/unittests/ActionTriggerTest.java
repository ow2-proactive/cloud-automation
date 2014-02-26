package unittests;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.brokering.Configuration;
import org.ow2.proactive.workflowcatalog.References;
import org.ow2.proactive.brokering.occi.Resource;
import org.ow2.proactive.brokering.occi.categories.trigger.ActionTrigger;
import org.ow2.proactive.brokering.triggering.ScriptUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActionTriggerTest {

    private static final Integer PERIODMS = 10;

    private static Integer trueActions = 0;
    private static Integer falseActions = 0;
    private static Integer initActions = 0;
    private static Integer stopActions = 0;

    @BeforeClass
    public static void before() throws Exception {
        Configuration config = new Configuration();
        config.actions = new Configuration.Actions();
        config.actions.path = ActionTrigger.class.getResource("/actions/").getFile();
        config.actions.refresh = 10;
        config.conditions = new Configuration.Conditions();
        config.conditions.path = ActionTrigger.class.getResource("/conditions/").getFile();
        config.conditions.refresh = 10;
        ActionTrigger.getInstance(config);
    }

    @Test
    public void actionTriggerStartScheduleOnce_EncodedScripts_Test() throws Exception {

        Map<String, String> actionTriggerAttributes =
                getCreationScheduleOnceActionTriggerAttributes();

        actionTriggerStartScheduleOnce(actionTriggerAttributes);

    }

    @Test
    public void actionTriggerStartScheduleOnce_FilenameScripts_Test() throws Exception {

        Map<String, String> actionTriggerAttributes =
                getCreationScheduleOnceActionTriggerAttributes();

        actionTriggerAttributes.put(ActionTrigger.OCCI_MONITORING_ACTION, "ActionTrueScript.groovy-script");

        actionTriggerStartScheduleOnce(actionTriggerAttributes);

    }

    private void actionTriggerStartScheduleOnce(Map<String, String> atts) throws Exception {

        ActionTrigger actionTrigger = ActionTrigger.getInstance();

        initializeCallbackCounters();

        Assert.assertTrue(trueActions == 0);
        Assert.assertTrue(falseActions == 0);

        References references = actionTrigger.request(
                Resource.ACTION_TRIGGER_CATEGORY_NAME, "create",
                "scheduleonce", atts);

        Thread.sleep(100 * PERIODMS);

        Assert.assertTrue(references.areAllSubmitted());
        Assert.assertTrue(trueActions == 1);
        Assert.assertTrue(falseActions == 0);

    }

    @Test
    public void actionTriggerStart_EncodedScripts_Test() throws Exception {
        String uuid = UUID.randomUUID().toString();
        Map<String, String> actionTriggerAttributes = getCreationActionTriggerAttributes(uuid);
        actionTriggerStart_BaseTest(actionTriggerAttributes, false);
    }

    @Test
    public void actionTriggerStart_FilenameScripts_Test() throws Exception {
        String uuid = UUID.randomUUID().toString();
        Map<String, String> atts = getCreationActionTriggerAttributes(uuid);

        atts.put(ActionTrigger.OCCI_CONDITION_SCRIPT, "ConditionScript.groovy-script");
        atts.put(ActionTrigger.OCCI_MONITORING_INITACTION, "ActionInitScript.groovy-script");
        atts.put(ActionTrigger.OCCI_MONITORING_FALSEACTION, "ActionFalseScript.groovy-script");
        atts.put(ActionTrigger.OCCI_MONITORING_TRUEACTION, "ActionTrueScript.groovy-script");
        atts.put(ActionTrigger.OCCI_MONITORING_STOPACTION, "ActionStopScript.groovy-script");

        actionTriggerStart_BaseTest(atts, true);
    }


    private void actionTriggerStart_BaseTest(Map<String, String> atts, boolean withStartStopScript) throws Exception {

        // This test will post a action trigger rule with 3 scripts: a condition
        // script, a true action script, and a false script.
        // If condition is true, true action script will be executed. This test
        // executes a true action script that increases a trueActions counter.
        // The test is based in counting if these variables are incremented or not.
        ActionTrigger actionTrigger = ActionTrigger.getInstance();

        initializeCallbackCounters();

        Assert.assertTrue(trueActions == 0);
        Assert.assertTrue(falseActions == 0);
        Assert.assertTrue(ActionTrigger.getTimers().size() == 0);

        actionTrigger.request(
                Resource.ACTION_TRIGGER_CATEGORY_NAME, "create",
                "schedule", atts); // rule started

        Thread.sleep(100 * PERIODMS);
        if (withStartStopScript) Assert.assertTrue(initActions == 1);
        Assert.assertTrue(trueActions > 20);
        Assert.assertTrue(falseActions > 20);
        if (withStartStopScript) Assert.assertTrue(stopActions == 0);
        Assert.assertTrue(ActionTrigger.getTimers().size() == 1);

        actionTrigger.request(
                Resource.ACTION_TRIGGER_CATEGORY_NAME, "update", "delete",
                atts); // rule stopped

        Thread.sleep(10 * PERIODMS);

        if (withStartStopScript) Assert.assertTrue(initActions == 1);
        if (withStartStopScript) Assert.assertTrue(stopActions == 1);

        initializeCallbackCounters();
        Thread.sleep(100 * PERIODMS);

        Assert.assertTrue(trueActions == 0);
        Assert.assertTrue(falseActions == 0);

        Assert.assertTrue(ActionTrigger.getTimers().size() == 0);

    }

    @Test
    public void actionTriggerStartScheduleWithMissingDelayArgument_Test() throws Exception {

        // Should show an error as there is one argument missing

        String uuid = UUID.randomUUID().toString();

        ActionTrigger actionTrigger = ActionTrigger.getInstance();

        Map<String, String> actionTriggerAttributes =
                getCreationActionTriggerAttributes(uuid);

        actionTriggerAttributes.remove(ActionTrigger.OCCI_MONITORING_PERIODMS);

        References references = actionTrigger.request(
                Resource.ACTION_TRIGGER_CATEGORY_NAME, "create",
                "schedule", actionTriggerAttributes);

        Assert.assertFalse(references.areAllSubmitted());
        Assert.assertTrue(references.getSummary().contains("delay"));
        Assert.assertTrue(references.getSummary().contains("null"));
    }


    private Map<String, String> getCreationScheduleOnceActionTriggerAttributes()
            throws IOException {
        Map<String, String> actionTriggerAttributes = new HashMap<String, String>();
        actionTriggerAttributes.put(
                ActionTrigger.OCCI_MONITORING_ACTION,
                getScriptAsEncodedString("/actions/ActionTrueScript.groovy-script"));
        actionTriggerAttributes.put(
                ActionTrigger.OCCI_CORE_ID,
                UUID.randomUUID().toString());
        return actionTriggerAttributes;
    }

    private Map<String, String> getCreationActionTriggerAttributes(String uuid) throws IOException {
        Map<String, String> actionTriggerAttributes = new HashMap<String, String>();
        actionTriggerAttributes.put(ActionTrigger.OCCI_CORE_ID, uuid);
        actionTriggerAttributes.put(ActionTrigger.OCCI_CONDITION_SCRIPT,
                                   getScriptAsEncodedString("/conditions/ConditionScript.groovy-script"));      // half times true, half times false
        actionTriggerAttributes.put(ActionTrigger.OCCI_MONITORING_FALSEACTION,
                                   getScriptAsEncodedString("/actions/ActionFalseScript.groovy-script"));
        actionTriggerAttributes.put(ActionTrigger.OCCI_MONITORING_TRUEACTION,
                                   getScriptAsEncodedString("/actions/ActionTrueScript.groovy-script"));
        actionTriggerAttributes.put(ActionTrigger.OCCI_MONITORING_PERIODMS, PERIODMS.toString());
        return actionTriggerAttributes;
    }

    private void initializeCallbackCounters() {
        trueActions = 0;
        falseActions = 0;
        stopActions = 0;
        initActions = 0;
    }

    private String getScriptAsEncodedString(String path) throws IOException {
        return ScriptUtils.encode(getScriptAsString(path));
    }

    private String getScriptAsString(String path) throws IOException {
        File f = new File(this.getClass().getResource(path).getFile());
        return FileUtils.readFileToString(f);
    }

    public static void addOneInitActionExecuted() {
        initActions++;
    }

    public static void addOneStopActionExecuted() {
        stopActions++;
    }

    public static void addOneFalseActionExecuted() {
        falseActions++;
    }

    public static void addOneTrueActionExecuted() {
        trueActions++;
    }

}

