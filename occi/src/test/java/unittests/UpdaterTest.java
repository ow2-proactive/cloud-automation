package unittests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.ow2.proactive.brokering.Updater;
import org.ow2.proactive.brokering.occi.Database;
import org.ow2.proactive.brokering.occi.OcciServer;
import org.ow2.proactive.brokering.occi.Resource;
import org.ow2.proactive.brokering.occi.api.Occi;
import org.ow2.proactive.workflowcatalog.Reference;
import org.ow2.proactive.workflowcatalog.utils.scheduling.JobNotFinishedException;
import org.ow2.proactive.workflowcatalog.utils.scheduling.JobStatusRetrievalException;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerProxy;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import junit.framework.Assert;
import org.apache.http.auth.AuthenticationException;
import org.junit.BeforeClass;
import org.junit.Test;

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

        // The first tasks results response is a expected response, where tasks have results
        // that are json formatted.
        // The second tasks results response is a unexpected response, where tasks have results
        // that are non-json formatted.
        Map<String, String> goodTaskResults = new HashMap<String, String>();
        goodTaskResults.put("task1", "{\"occi.compute.state\":\"up\",\"occi.compute.hostname\":\"pepa\"}");
        goodTaskResults.put("task2", "{\"occi.compute.cores\":\"1\",\"occi.compute.memory\":\"1024\"}");
        mockJobResults(scheduler, 1, goodTaskResults);

        Map<String, String> badTaskResults = new HashMap<String, String>();
        badTaskResults.put("task1", "nonJsonTaskResult1");
        badTaskResults.put("task2", "nonJsonTaskResult2");
        mockJobResults(scheduler, 2, badTaskResults);
        return scheduler;
    }

    private static void mockJobResults(SchedulerProxy scheduler,
      int jobId, Map<String, String> taskResults) throws AuthenticationException, JobNotFinishedException, JobStatusRetrievalException {
        JobIdData jobIdData = new JobIdData();
        jobIdData.setId(jobId);
        jobIdData.setReadableName("TestJob" + jobId);
        Reference jobReference = Reference.buildJobReference("", jobIdData);
        jobReferences.add(jobReference);
        when(scheduler.getAllTaskResults(jobReference.getId())).thenReturn(taskResults);
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

}
