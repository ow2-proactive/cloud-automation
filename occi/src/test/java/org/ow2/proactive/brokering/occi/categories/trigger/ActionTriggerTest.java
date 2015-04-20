/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 2013-2015 ActiveEon
 * 
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * $$ACTIVEEON_INITIAL_DEV$$
 */


package org.ow2.proactive.brokering.occi.categories.trigger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.ow2.proactive.brokering.occi.client.ActionTriggerHandler;
import org.ow2.proactive.brokering.triggering.ScriptUtils;
import org.ow2.proactive.workflowcatalog.References;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class ActionTriggerTest {

    private static final Integer PERIODMS = 10;

    private static Integer trueActions = 0;
    private static Integer falseActions = 0;
    private static Integer initActions = 0;
    private static Integer stopActions = 0;

    private static String actionsPath  = ActionTriggerTest.class.getResource("/actions/").getFile();
    private static String conditionsPath  = ActionTriggerTest.class.getResource("/conditions/").getFile();

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

        actionTriggerAttributes.put(ActionTrigger.OCCI_MONITORING_ACTION, "ActionTrueScript.groovy");

        actionTriggerStartScheduleOnce(actionTriggerAttributes);

    }

    private void actionTriggerStartScheduleOnce(Map<String, String> atts) throws Exception {

        ActionTriggerHandler actionTrigger = new ActionTriggerHandler(actionsPath, 10, conditionsPath, 10);

        initializeCallbackCounters();

        Assert.assertTrue(trueActions == 0);
        Assert.assertTrue(falseActions == 0);

        References references = actionTrigger.request(
          "create",
          ActionTriggerHandler.ACTION_SCHEDULE_ONCE, atts);

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

        atts.put(ActionTrigger.OCCI_CONDITION_SCRIPT, "ConditionScript.groovy");
        atts.put(ActionTrigger.OCCI_MONITORING_INITACTION, "ActionInitScript.groovy");
        atts.put(ActionTrigger.OCCI_MONITORING_FALSEACTION, "ActionFalseScript.groovy");
        atts.put(ActionTrigger.OCCI_MONITORING_TRUEACTION, "ActionTrueScript.groovy");
        atts.put(ActionTrigger.OCCI_MONITORING_STOPACTION, "ActionStopScript.groovy");

        actionTriggerStart_BaseTest(atts, true);
    }


    private void actionTriggerStart_BaseTest(Map<String, String> atts, boolean withStartStopScript) throws Exception {

        // This test will post a action trigger rule with 3 scripts: a condition
        // script, a true action script, and a false script.
        // If condition is true, true action script will be executed. This test
        // executes a true action script that increases a trueActions counter.
        // The test is based in counting if these variables are incremented or not.
        ActionTriggerHandler actionTrigger = new ActionTriggerHandler(actionsPath, 10, conditionsPath, 10);

        initializeCallbackCounters();

        Assert.assertTrue(trueActions == 0);
        Assert.assertTrue(falseActions == 0);
        Assert.assertTrue(actionTrigger.getTimers().size() == 0);

        actionTrigger.request(
          "create",
          ActionTriggerHandler.ACTION_SCHEDULE, atts); // rule started

        Thread.sleep(100 * PERIODMS);
        if (withStartStopScript) Assert.assertTrue(initActions == 1);
        Assert.assertTrue(trueActions > 20);
        Assert.assertTrue(falseActions > 20);
        if (withStartStopScript) Assert.assertTrue(stopActions == 0);
        Assert.assertTrue(actionTrigger.getTimers().size() == 1);

        actionTrigger.request(
          "update", "delete",
          atts); // rule stopped

        Thread.sleep(10 * PERIODMS);

        if (withStartStopScript) Assert.assertTrue(initActions == 1);
        if (withStartStopScript) Assert.assertTrue(stopActions == 1);

        initializeCallbackCounters();
        Thread.sleep(100 * PERIODMS);

        Assert.assertTrue(trueActions == 0);
        Assert.assertTrue(falseActions == 0);

        Assert.assertTrue(actionTrigger.getTimers().size() == 0);

    }

    @Test
    public void actionTriggerStartScheduleWithMissingDelayArgument_Test() throws Exception {

        // Should show an error as there is one argument missing

        String uuid = UUID.randomUUID().toString();

        ActionTriggerHandler actionTrigger = new ActionTriggerHandler(actionsPath, 10, conditionsPath, 10);

        Map<String, String> actionTriggerAttributes =
          getCreationActionTriggerAttributes(uuid);

        actionTriggerAttributes.remove(ActionTrigger.OCCI_MONITORING_PERIODMS);

        References references = actionTrigger.request(
          "create",
          ActionTriggerHandler.ACTION_SCHEDULE, actionTriggerAttributes);

        Assert.assertFalse(references.areAllSubmitted());
        Assert.assertTrue(references.getSummary().contains("delay"));
        Assert.assertTrue(references.getSummary().contains("null"));
    }


    private Map<String, String> getCreationScheduleOnceActionTriggerAttributes()
      throws IOException {
        Map<String, String> actionTriggerAttributes = new HashMap<String, String>();
        actionTriggerAttributes.put(
          ActionTrigger.OCCI_MONITORING_ACTION,
          getScriptAsEncodedString("/actions/ActionTrueScript.groovy"));
        actionTriggerAttributes.put(
          ActionTrigger.OCCI_CORE_ID,
          UUID.randomUUID().toString());
        return actionTriggerAttributes;
    }

    private Map<String, String> getCreationActionTriggerAttributes(String uuid) throws IOException {
        Map<String, String> actionTriggerAttributes = new HashMap<String, String>();
        actionTriggerAttributes.put(ActionTrigger.OCCI_CORE_ID, uuid);
        actionTriggerAttributes.put(ActionTrigger.OCCI_CONDITION_SCRIPT,
          getScriptAsEncodedString("/conditions/ConditionScript.groovy"));      // half times true, half times false
        actionTriggerAttributes.put(ActionTrigger.OCCI_MONITORING_FALSEACTION,
          getScriptAsEncodedString("/actions/ActionFalseScript.groovy"));
        actionTriggerAttributes.put(ActionTrigger.OCCI_MONITORING_TRUEACTION,
          getScriptAsEncodedString("/actions/ActionTrueScript.groovy"));
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

