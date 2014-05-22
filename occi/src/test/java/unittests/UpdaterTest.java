package unittests;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.ow2.proactive.brokering.Updater;
import org.ow2.proactive.brokering.occi.Database;
import org.ow2.proactive.brokering.occi.OcciServer;
import org.ow2.proactive.brokering.occi.Resource;
import org.ow2.proactive.brokering.occi.ResourcesHandler;
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

    private static final String COMPUTE_NAME = "compute";

    private static final Long UPDATE_PERIOD = TimeUnit.MILLISECONDS.toMillis(1);

    private static final Integer JOB_ID_ALL_TASKS_PRODUCE_JSON = 0;
    private static final Integer JOB_ID_NO_TASK_PRODUCES_JSON = 1;
    private static final Integer JOB_ID_SOME_TASKS_PRODUCE_JSON = 2;
    private static final Integer JOB_ID_TASKS_WITH_ERROR = 3;

    private static final String ERROR_STRING = "Exception";
    private static final String TASK_RESULT_WHEN_ERROR = ERROR_STRING + ": , = '' \"\"...";

    private static Occi occiServer;
    private static SchedulerProxy scheduler;
    private static Map<Integer, Reference> jobReferences = new HashMap<Integer, Reference>();

    @BeforeClass
    public static void beforeClass() throws Exception {
        Database.setDatabaseName("occi-updater-test");
        new OcciServer();
        scheduler = createMockOfSchedulerProxy();
        occiServer = createMockOfOcciServer();
    }

    @Test
    public void verifyJsonTaskOutputsAreInsertedIntoResourceProperties_Test() throws Exception {

        // A resource is created, and a job is submitted consequently for real stuff.
        // The output of the job's tasks must be used to update the resource's properties.

        Resource resource = ResourcesHandler.factory(
                UUID.randomUUID().toString(),
                COMPUTE_NAME,
                getDefaultComputeAttributes());
        Reference jobReference = jobReferences.get(JOB_ID_ALL_TASKS_PRODUCE_JSON);

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

        Resource resource = ResourcesHandler.factory(
                UUID.randomUUID().toString(),
                COMPUTE_NAME,
                getDefaultComputeAttributes());
        Reference jobReference = jobReferences.get(JOB_ID_NO_TASK_PRODUCES_JSON);

        Updater updater = new Updater(occiServer, scheduler, UPDATE_PERIOD);
        updater.addResourceToTheUpdateQueue(jobReference, resource);

        assertThereIsOneJobPendingForUpdate(updater);

        waitUntilUpdaterReacts();

        // if finished but no taskresult in json format, it should remove the
        // update entry from the update queue
        Assert.assertTrue(updater.getUpdateQueueSize() == 0);

    }

    @Test
    public void verifyAllOuputJsonTaskOutputAreInsertedIntoResourcePropertiesEvenIfNotAllTaskOutputsAreJson_Test()
            throws Exception {

        Resource resource = ResourcesHandler.factory(
                UUID.randomUUID().toString(),
                COMPUTE_NAME,
                getDefaultComputeAttributes());
        Reference jobReference = jobReferences.get(JOB_ID_SOME_TASKS_PRODUCE_JSON);

        Updater updater = new Updater(occiServer, scheduler, UPDATE_PERIOD);
        updater.addResourceToTheUpdateQueue(jobReference, resource);

        assertThereIsOneJobPendingForUpdate(updater);

        waitUntilUpdaterReacts();

        assertSomeResourceAttributesAreCorrectlyTakenFromTaskResults(resource);

        Assert.assertTrue(updater.getUpdateQueueSize() == 0);

    }

    @Test
    public void verifyResourcePropertiesAreUpdatedIfJobError_Test()
            throws Exception {

        Resource resource = ResourcesHandler.factory(
                UUID.randomUUID().toString(),
                COMPUTE_NAME,
                getDefaultComputeAttributes());
        Reference jobReference = jobReferences.get(JOB_ID_TASKS_WITH_ERROR);

        Updater updater = new Updater(occiServer, scheduler, UPDATE_PERIOD);
        updater.addResourceToTheUpdateQueue(jobReference, resource);

        assertThereIsOneJobPendingForUpdate(updater);

        waitUntilUpdaterReacts();

        assertSomeResourceAttributesAreCorrectlyTakenFromTaskResults(resource);
        assertResourceAttributesErrorDescriptionContains(resource, ERROR_STRING);

        Assert.assertTrue(updater.getUpdateQueueSize() == 0);

    }

    // PRIVATE METHODS

    private static Occi createMockOfOcciServer() {
        return new OcciServer();
    }

    private static SchedulerProxy createMockOfSchedulerProxy() throws Exception {
        SchedulerProxy scheduler = mock(SchedulerProxy.class);

        {
            // The first tasks results response is a expected response, where tasks have results
            // that are json formatted.
            Map<String, String> goodTaskResults = new HashMap<String, String>();
            goodTaskResults.put("task1", "{\"occi.compute.state\":\"up\",\"occi.compute.hostname\":\"pepa\"}");
            goodTaskResults.put("task2", "{\"occi.compute.cores\":\"1\",\"occi.compute.memory\":\"1024\"}");
            mockJobResults(scheduler, JOB_ID_ALL_TASKS_PRODUCE_JSON, goodTaskResults);
        }

        {
            // The second tasks results response is a unexpected response, where tasks have results
            // that are non-json formatted.
            Map<String, String> badTaskResults = new HashMap<String, String>();
            badTaskResults.put("task1", "nonJsonTaskResult1");
            badTaskResults.put("task2", "nonJsonTaskResult2");
            mockJobResults(scheduler, JOB_ID_NO_TASK_PRODUCES_JSON, badTaskResults);
        }

        {
            // The third tasks results response is a sort of expected response, where some tasks have results
            // that are json formatted and some do not.
            Map<String, String> mixedTaskResults = new HashMap<String, String>();
            mixedTaskResults.put("task1", "{\"occi.compute.state\":\"up\",\"occi.compute.hostname\":\"pepa\"}");
            mixedTaskResults.put("task2", "nonJsonTaskResult2");
            mockJobResults(scheduler, JOB_ID_SOME_TASKS_PRODUCE_JSON, mixedTaskResults);
        }

        {
            // The fourth tasks results response is a non expected response, where some tasks fail.
            Map<String, String> failedTaskResults = new HashMap<String, String>();
            failedTaskResults.put("task1", "{\"occi.compute.state\":\"up\",\"occi.compute.hostname\":\"pepa\"}");
            failedTaskResults.put("task2", "nonJsonTaskResult2");
            failedTaskResults.put("task3", TASK_RESULT_WHEN_ERROR);
            mockJobResults(scheduler, JOB_ID_TASKS_WITH_ERROR, failedTaskResults);
        }

        return scheduler;
    }

    private static void mockJobResults(SchedulerProxy scheduler,
      int jobId, Map<String, String> taskResults) throws AuthenticationException, JobNotFinishedException, JobStatusRetrievalException {
        JobIdData jobIdData = new JobIdData();
        jobIdData.setId(jobId);
        jobIdData.setReadableName("TestJob" + jobId);
        Reference jobReference = Reference.buildJobReference("", jobIdData);
        jobReferences.put(jobId, jobReference);
        when(scheduler.getAllTaskResults(jobReference.getId())).thenReturn(taskResults);
    }

    private Map<String, String> getDefaultComputeAttributes() {
        return new HashMap<String, String>();
    }

    private void waitUntilUpdaterReacts() throws InterruptedException {
        Thread.sleep(1000);
    }

    private void assertResourceAttributesAreCorrectlyTakenFromTaskResults(Resource resource) {
        Assert.assertTrue(resource.getAttributes().get("occi.compute.state").equalsIgnoreCase("up"));
        Assert.assertTrue(resource.getAttributes().get("occi.compute.cores").equalsIgnoreCase("1"));
        Assert.assertTrue(resource.getAttributes().get("occi.compute.memory").equalsIgnoreCase("1024"));
        Assert.assertTrue(resource.getAttributes().get("occi.compute.hostname").equalsIgnoreCase("pepa"));
    }

    private void assertSomeResourceAttributesAreCorrectlyTakenFromTaskResults(Resource resource) {
        Assert.assertTrue(resource.getAttributes().get("occi.compute.state").equalsIgnoreCase("up"));
        Assert.assertTrue(resource.getAttributes().get("occi.compute.hostname").equalsIgnoreCase("pepa"));
    }

    private void assertResourceAttributesErrorDescriptionContains(Resource resource, String message) {
        Assert.assertTrue(resource.getAttributes().get("occi.error.description").contains(message));
    }

    private void assertThereIsOneJobPendingForUpdate(Updater updater) {
        Assert.assertTrue(updater.getUpdateQueueSize() == 1);
    }


}
