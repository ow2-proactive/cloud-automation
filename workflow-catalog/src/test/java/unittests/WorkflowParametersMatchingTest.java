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

