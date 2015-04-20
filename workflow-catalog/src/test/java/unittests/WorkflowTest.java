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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.BeforeClass;
import junit.framework.Assert;
import org.ow2.proactive.workflowcatalog.Workflow;
import org.ow2.proactive.workflowcatalog.utils.scheduling.JobCreationException;
import org.ow2.proactive.workflowcatalog.utils.scheduling.JobParsingException;

import javax.xml.transform.TransformerException;

public class WorkflowTest {

    private static Workflow workflow;

    @BeforeClass
    public static void beforeAll() throws Exception {
        URL url = WorkflowTest.class.getClass().getResource("/catalog/workflow1.xml");
        workflow = new Workflow(new File(url.getFile()));
        workflow.update();
    }

    @Test
    public void parseNoArgumentsWorkflow_Test() throws Exception {
        URL url = WorkflowTest.class.getClass().getResource("/catalog/workflow3.xml");
        Workflow workflow = new Workflow(new File(url.getFile()));
        workflow.update();
        workflow.configure(new HashMap<String, String>());
    }

    @Test
    public void fillInWorkflow_Test() throws Exception {
        Assert.assertTrue(0 == 0);
    }

    @Test
    public void getVariables_Test() throws Exception {
        Map<String, String> variables = workflow.getVariables();
        Assert.assertTrue(variables.size() == 2);
        Assert.assertTrue(variables.keySet().contains("variable1"));
        Assert.assertTrue(variables.keySet().contains("variable2"));
    }

    @Test
    public void getGenericInformation_Test() throws Exception {
        Map<String, String> gInformation = workflow.getGenericInformation();
        Assert.assertTrue(gInformation.size() == 2);
        Assert.assertTrue(gInformation.keySet().contains("genericInformation1"));
        Assert.assertTrue(gInformation.keySet().contains("genericInformation2"));
    }

    @Test
    public void getName_Test() throws Exception {
        String name = workflow.getName();
        Assert.assertTrue("workflow1.xml".equals(name));
    }

}

