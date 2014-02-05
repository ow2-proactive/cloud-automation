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
    public void trivialMatch_Test() throws Exception {

        WorkflowParameters param1 = new WorkflowParameters();
        param1.setWorkflowName("workflow1.xml");
        param1.getVariables().put("genericInformation1", "genericInformationValue1");
        param1.getVariables().put("genericInformation2", "genericInformationValue2");
        param1.getVariables().put("variable1", "variableValue1");
        param1.getVariables().put("variable2", "variableValue2");
        Assert.assertTrue(param1.matches(workflow));

    }

    @Test
    public void nameRegexMatch_Test() throws Exception {

        WorkflowParameters param1 = new WorkflowParameters();
        param1.setWorkflowName("workflow.*");
        param1.getVariables().put("genericInformation1", "genericInformationValue1");
        param1.getVariables().put("genericInformation2", "genericInformationValue2");
        Assert.assertTrue(param1.matches(workflow));

        param1.setWorkflowName("not-matching-regex");
        Assert.assertFalse(param1.matches(workflow));

    }

    @Test
    public void genericInformationMatch_Test() throws Exception {

        WorkflowParameters param1 = new WorkflowParameters();
        param1.setWorkflowName("workflow1.xml");
        param1.getVariables().put("genericInformation1", "some-not-matching-value");
        param1.getVariables().put("genericInformation2", "genericInformationValue2");
        Assert.assertFalse(param1.matches(workflow));

        param1.getVariables().put("genericInformation1", "genericInformationValue1");
        Assert.assertTrue(param1.matches(workflow));

        param1.getVariables().remove("genericInformation1");
        Assert.assertFalse(param1.matches(workflow));

    }

}

