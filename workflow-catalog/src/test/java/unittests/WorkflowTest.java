package unittests;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.workflowcatalog.Workflow;

import java.io.File;
import java.net.URL;
import java.util.List;

public class WorkflowTest {

    private static Workflow workflow;

    @BeforeClass
    public static void beforeAll() throws Exception {
        URL url = WorkflowTest.class.getClass().getResource("/catalog/workflow1.xml");
        workflow = new Workflow(new File(url.getFile()));
    }

    @Test
    public void fillInWorkflow_Test() throws Exception {
        Assert.assertTrue(0 == 0);
    }

    @Test
    public void getVariables_Test() throws Exception {
        List<String> variables = workflow.listVariables();
        Assert.assertTrue(variables.size() == 2);
        Assert.assertTrue(variables.contains("variable1"));
        Assert.assertTrue(variables.contains("variable2"));
    }

    @Test
    public void getGenericInformation_Test() throws Exception {
        List<String> gInformation = workflow.listGenericInformation();
        Assert.assertTrue(gInformation.size() == 2);
        Assert.assertTrue(gInformation.contains("genericInformation1"));
        Assert.assertTrue(gInformation.contains("genericInformation2"));
    }

    @Test
    public void getName_Test() throws Exception {
        String name = workflow.getName();
        Assert.assertTrue("workflow1.xml".equals(name));
    }

}

