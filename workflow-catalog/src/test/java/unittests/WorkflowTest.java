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

