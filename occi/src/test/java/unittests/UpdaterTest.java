package unittests;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.workflowcatalog.Reference;
import org.ow2.proactive.brokering.Updater;
import org.ow2.proactive.brokering.occi.Database;
import org.ow2.proactive.brokering.occi.OcciServer;
import org.ow2.proactive.brokering.occi.Resource;
import org.ow2.proactive.brokering.occi.api.Occi;
import org.ow2.proactive.brokering.occi.categories.Utils;
import org.ow2.proactive.workflowcatalog.utils.scheduling.JobSubmissionResponse;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerProxy;

import javax.json.JsonObject;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UpdaterTest {

    private static final String HOST = "host";
    private static final String COMPUTE_NAME = "compute";
    private static final Long UPDATE_PERIOD = TimeUnit.MILLISECONDS.toMillis(1);

    private static List<Reference> jobReferences = new ArrayList<Reference>();

    private static SchedulerProxy scheduler;
    private static Occi occiServer;

    @BeforeClass
    public static void beforeClass() throws Exception {
        OcciServer.setDatabase(mock(Database.class));
        new OcciServer();
        scheduler = createMockOfSchedulerProxy();
        occiServer = createMockOfOcciServer();
    }

    @Test
    public void verifyJsonTaskOutputsAreInsertedIntoResourceProperties_Test() throws Exception {

        Reference jobReference = getJobResultWithJsonTasksOutput();
        Resource resource = Resource.factory(
                UUID.randomUUID(),
                COMPUTE_NAME,
                getDefaultComputeAttributes());

        Updater updater = new Updater(occiServer, scheduler, UPDATE_PERIOD);
        updater.addResourceToTheUpdateQueue(jobReference, resource);

        Assert.assertTrue(updater.getUpdateQueueSize() == 1);

        waitUntilUpdaterReacts();

        assertResourceAttributesAreCorrectlyTakenFromTaskResults(resource);

        Assert.assertTrue(updater.getUpdateQueueSize() == 0);
    }

    @Test
    public void verifyOuputJsonTaskOutputIsInsertedIntoResourcePropertiesIfTaskOutputIsNotJson_Test()
            throws Exception {

        Reference jobReference = getJobResultWithNonJsonTasksOutput();
        Resource resource = Resource.factory(
                UUID.randomUUID(),
                COMPUTE_NAME,
                getDefaultComputeAttributes());

        Updater updater = new Updater(occiServer, scheduler, UPDATE_PERIOD);
        updater.addResourceToTheUpdateQueue(jobReference, resource);

        assertThereIsOneJobPendingForUpdate(updater);

        waitUntilUpdaterReacts();

        // if finished but no taskresult in json format, it should remove the
        // update entry from the update queue
        Assert.assertTrue(updater.getUpdateQueueSize() == 0);

    }

    private void assertThereIsOneJobPendingForUpdate(Updater updater) {
        Assert.assertTrue(updater.getUpdateQueueSize() == 1);
    }

    // PRIVATE METHODS

    private Reference getJobResultWithJsonTasksOutput() {
        return jobReferences.get(0);
    }

    private Reference getJobResultWithNonJsonTasksOutput() {
        return jobReferences.get(1);
    }

    private static Occi createMockOfOcciServer() {
        return new OcciServer();
    }

    private static SchedulerProxy createMockOfSchedulerProxy() throws Exception {
        SchedulerProxy scheduler = mock(SchedulerProxy.class);

        Properties restResponses = new Properties();
        restResponses.load(UpdaterTest.class.getResourceAsStream("/properties/updater.properties"));

        // The first tasks results response is a expected response, where tasks have results
        // that are json formatted.
        // The second tasks results response is a unexpected response, where tasks have results
        // that are non-json formatted.
        for (int i = 1; i <= 2; i++) {
            Reference jobReference = Reference.buildJobReference("", new JobSubmissionResponse(createSubmitResponse(i, "TestJob" + i)));
            jobReferences.add(jobReference);
            JsonObject taskRes = Utils.convertToJson(restResponses.get(i + "").toString());
            when(scheduler.getAllTaskResultsAsJson(jobReference)).thenReturn(taskRes);
        }
        return scheduler;
    }

    private Map<String, String> getDefaultComputeAttributes() {
        return new HashMap<String, String>();
    }

    private void waitUntilUpdaterReacts() throws InterruptedException {
        Thread.sleep(100);
    }

    private void assertResourceAttributesAreCorrectlyTakenFromTaskResults(Resource resource) {
        Assert.assertTrue(resource.getAttributes().get("occi.compute.state").equalsIgnoreCase("up"));
        Assert.assertTrue(resource.getAttributes().get("occi.compute.cores").equalsIgnoreCase("1"));
        Assert.assertTrue(resource.getAttributes().get("occi.compute.memory").equalsIgnoreCase("1024"));
        Assert.assertTrue(resource.getAttributes().get("occi.compute.hostname").equalsIgnoreCase("pepa"));
    }

    public static String createSubmitResponse(int jobId, String readableName) {
        return "{\"id\":" + jobId + ",\"readableName\":\"" + readableName + "\"}";
    }

}
