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


package unittests;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.workflowcatalog.Workflow;
import org.ow2.proactive.workflowcatalog.WorkflowParameters;

import java.io.File;
import java.net.URL;

public class WorkflowParametersMatchingTest {

    private static Workflow workflow;

    @BeforeClass
    public static void beforeAll() throws Exception {
        URL url = WorkflowTest.class.getClass().getResource("/catalog/workflow1.xml");
        workflow = new Workflow(new File(url.getFile()));
        workflow.update();
    }

    @Test
    public void trivialMatchRelaxed_Test() throws Exception {

        WorkflowParameters param1 = new WorkflowParameters();
        param1.setStrictMatch(false);
        param1.setName("workflow1.xml");
        Assert.assertTrue(param1.matches(workflow));

        WorkflowParameters param2 = new WorkflowParameters();
        param2.setStrictMatch(false);
        param2.setName("workflow2.xml");
        Assert.assertFalse(param2.matches(workflow));

    }

    @Test
    public void trivialMatchStrict_Test() throws Exception {

        WorkflowParameters param1 = new WorkflowParameters();
        param1.setStrictMatch(true);
        param1.setName("workflow1.xml");
        param1.getGenericInformation().put("genericInformation1", "genericInformationValue1");
        param1.getGenericInformation().put("genericInformation2", "genericInformationValue2");
        param1.getVariables().put("variable1", "variableValue1");
        param1.getVariables().put("variable2", "variableValue2");
        Assert.assertTrue(param1.matches(workflow));

    }

    @Test
    public void nameRegexMatchStrict_Test() throws Exception {

        WorkflowParameters param1 = new WorkflowParameters();
        param1.setStrictMatch(true);
        param1.setName("workflow.*");
        param1.getGenericInformation().put("genericInformation1", "genericInformationValue1");
        param1.getGenericInformation().put("genericInformation2", "genericInformationValue2");
        param1.getVariables().put("variable1", "smth");
        param1.getVariables().put("variable2", "smth");
        Assert.assertTrue(param1.matches(workflow));

        param1.setName("not-matching-regex");
        Assert.assertFalse(param1.matches(workflow));

    }

    @Test
    public void genericInformationMatchStrict_Test() throws Exception {

        WorkflowParameters param1 = new WorkflowParameters();
        param1.setStrictMatch(true);
        param1.setName("workflow1.xml");
        param1.getGenericInformation().put("genericInformation1", "some-not-matching-value");
        param1.getGenericInformation().put("genericInformation2", "genericInformationValue2");
        param1.getVariables().put("variable1", "smth");
        param1.getVariables().put("variable2", "smth");
        Assert.assertFalse(param1.matches(workflow));

        param1.getGenericInformation().put("genericInformation1", "genericInformationValue1");
        Assert.assertTrue(param1.matches(workflow));

        param1.getGenericInformation().remove("genericInformation1");
        Assert.assertFalse(param1.matches(workflow));

    }

}

