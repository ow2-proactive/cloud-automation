package unittests;

import junit.framework.Assert;
import net.minidev.json.JSONObject;
import org.apache.http.auth.AuthenticationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.brokering.Configuration;
import org.ow2.proactive.brokering.occi.OcciServer;
import org.ow2.proactive.brokering.occi.Resource;
import org.ow2.proactive.brokering.occi.ResourcesHandler;
import org.ow2.proactive.brokering.occi.api.Occi;
import org.ow2.proactive.brokering.occi.categories.Utils;
import org.ow2.proactive.brokering.occi.database.Database;
import org.ow2.proactive.brokering.occi.database.DatabaseFactory;
import org.ow2.proactive.brokering.updater.Updater;
import org.ow2.proactive.brokering.updater.requests.UpdaterRequest;
import org.ow2.proactive.workflowcatalog.Reference;
import org.ow2.proactive.workflowcatalog.utils.scheduling.JobNotFinishedException;
import org.ow2.proactive.workflowcatalog.utils.scheduling.JobStatusRetrievalException;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerProxy;
import org.ow2.proactive.workflowcatalog.utils.scheduling.TasksResults;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UpdaterTest {

    private static final String COMPUTE_NAME = "compute";
    private static final String PLATFORM_NAME = "platform";
    private static final String BROKER_URL = "http://test.cloudautomation.com/cloudautomation/";
    private static final String UUID_REGEX = "[\\w]{8}-[\\w]{4}-[\\w]{4}-[\\w]{4}-[\\w]{12}";

    private static final Long UPDATE_PERIOD = TimeUnit.MILLISECONDS.toMillis(1);

    private static final String JOB_ID = "0";

    private static final String ERROR_STRING = "Exception";
    private static final String TASK_RESULT_WHEN_ERROR = ERROR_STRING + ": , = '' \"\"...";

    private static Occi occiServer;
    private static SchedulerProxy scheduler;
    private static Map<String, Reference> jobReferences = new HashMap<String, Reference>();

    @BeforeClass
    public static void beforeClass() throws Exception {
        Random r = new Random();
        Database db = mock(Database.class);
        DatabaseFactory.mockupWith(db);

        scheduler = createMockOfSchedulerProxy();
        occiServer = createMockOfOcciServer(scheduler);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        DatabaseFactory.mockupWith(null);
    }

    @Test
    public void verifyJsonTaskOutputsAreInsertedIntoResourceProperties_Test() throws Exception {

        // A resource is created, and a job is submitted consequently for real stuff.
        // The output of the job's tasks must be used to update the resource's properties.

        Resource resource = ResourcesHandler.factory(
                UUID.randomUUID().toString(),
                COMPUTE_NAME,
                getDefaultResourceAttributes());

        {
            // This tasks results response is a expected response, where tasks have results
            // that are json formatted.
            Map<String, String> goodTaskResults = new HashMap<String, String>();
            goodTaskResults.put("task1", buildDefaultJsonTaskResult().toJSONString());
            goodTaskResults.put("task2", buildNonDefaultJsonTaskResult().toJSONString());
            mockJobResults(scheduler, JOB_ID, new TasksResults(goodTaskResults));
        }
        Reference jobReference = jobReferences.get(JOB_ID);

        Updater updater = new Updater(occiServer, scheduler, UPDATE_PERIOD);
        updater.addResourceToTheUpdateQueue(jobReference, resource);

        assertThereIsOneJobPendingForUpdate(updater);

        waitUntilUpdaterReacts(updater);

        assertResourceAttributesAreCorrectlyTakenFromTaskResults(resource);

    }

    @Test
    public void verifyOuputJsonTaskOutputIsInsertedIntoResourcePropertiesIfTaskOutputIsNotJson_Test()
            throws Exception {

        Resource resource = ResourcesHandler.factory(
                UUID.randomUUID().toString(),
                COMPUTE_NAME,
                getDefaultResourceAttributes());

        {
            // This tasks results response is a unexpected response, where tasks have results
            // that are non-json formatted.

            Map<String, String> badTaskResults = new HashMap<String, String>();
            badTaskResults.put("task1", "nonJsonTaskResult1");
            badTaskResults.put("task2", "nonJsonTaskResult2");
            mockJobResults(scheduler, JOB_ID, new TasksResults(badTaskResults));
        }
        Reference jobReference = jobReferences.get(JOB_ID);

        Updater updater = new Updater(occiServer, scheduler, UPDATE_PERIOD);
        updater.addResourceToTheUpdateQueue(jobReference, resource);

        assertThereIsOneJobPendingForUpdate(updater);

        waitUntilUpdaterReacts(updater);

    }

    @Test
    public void verifyAllOuputJsonTaskOutputAreInsertedIntoResourcePropertiesEvenIfNotAllTaskOutputsAreJson_Test()
            throws Exception {

        Resource resource = ResourcesHandler.factory(
                UUID.randomUUID().toString(),
                COMPUTE_NAME,
                getDefaultResourceAttributes());

        {
            // This tasks results response is a sort of expected response, where some tasks have results
            // that are json formatted and some do not.
            Map<String, String> mixedTaskResults = new HashMap<String, String>();
            mixedTaskResults.put("task1", buildDefaultJsonTaskResult().toJSONString());
            mixedTaskResults.put("task2", "nonJsonTaskResult2");
            mockJobResults(scheduler, JOB_ID, new TasksResults(mixedTaskResults));
        }

        Reference jobReference = jobReferences.get(JOB_ID);

        Updater updater = new Updater(occiServer, scheduler, UPDATE_PERIOD);
        updater.addResourceToTheUpdateQueue(jobReference, resource);

        assertThereIsOneJobPendingForUpdate(updater);

        waitUntilUpdaterReacts(updater);

        assertSomeResourceAttributesAreCorrectlyTakenFromTaskResults(resource);

    }

    @Test
    public void verifyResourcePropertiesAreUpdatedIfJobError_Test()
            throws Exception {

        Resource resource = ResourcesHandler.factory(
                UUID.randomUUID().toString(),
                COMPUTE_NAME,
                getDefaultResourceAttributes());

        {
            // This tasks results response is a non expected response, where some tasks fail.
            Map<String, String> failedTaskResults = new HashMap<String, String>();
            failedTaskResults.put("task1", buildDefaultJsonTaskResult().toJSONString());
            failedTaskResults.put("task2", "nonJsonTaskResult2");
            failedTaskResults.put("task3", TASK_RESULT_WHEN_ERROR);
            mockJobResults(scheduler, JOB_ID, new TasksResults(failedTaskResults));
        }

        Reference jobReference = jobReferences.get(JOB_ID);

        Updater updater = new Updater(occiServer, scheduler, UPDATE_PERIOD);
        updater.addResourceToTheUpdateQueue(jobReference, resource);

        assertThereIsOneJobPendingForUpdate(updater);

        waitUntilUpdaterReacts(updater);

        assertSomeResourceAttributesAreCorrectlyTakenFromTaskResults(resource);
        assertResourceAttributesErrorDescriptionContains(resource, ERROR_STRING);

    }

    @Test
    public void verifyResourceCreation_Test() throws Exception {

        // We create a compute instance, that should create also a storage instance.
        // The location url of the storage instance should be put in the
        // PARENT_UPDATE_LOCATION_ATTRIBUTE of the compute resource.

        String PARENT_UPDATE_LOCATION_ATTRIBUTE = "occi.compute.localstorage";

        Resource resource = ResourcesHandler.factory(
                UUID.randomUUID().toString(),
                COMPUTE_NAME,
                getDefaultResourceAttributes());

        {

            JSONObject create = new JSONObject();
            create.put(UpdaterRequest.CATEGORY_KEY, "storage");
            create.put(UpdaterRequest.ACTION_KEY, "start");
            create.put(UpdaterRequest.PARENT_UPDATE_LOCATION_KEY, PARENT_UPDATE_LOCATION_ATTRIBUTE);

            // This tasks results response is a resource creation response, a new resource should be created
            Map<String, String> creationTaskResults = new HashMap<String, String>();
            creationTaskResults.put("task1", buildResourceCreationJsonTaskResult(create).toJSONString());
            mockJobResults(scheduler, JOB_ID, new TasksResults(creationTaskResults));

            // This submission is expected when creating the new resource
            when(scheduler.submitJob((File)notNull())).thenReturn(new JobIdData());
        }

        Reference jobReference = jobReferences.get(JOB_ID);

        Integer storages = getAmountOfInstancesForACategory("storage");
        assertAmountOfInstancesForACategory("storage", storages + 0);

        Updater updater = new Updater(occiServer, scheduler, UPDATE_PERIOD);
        updater.addResourceToTheUpdateQueue(jobReference, resource);

        assertThereIsOneJobPendingForUpdate(updater);

        waitUntilUpdaterReacts(updater);
        assertParentUpdateLocationAttributeIsAValidResourceLocation(resource,
                                                                    PARENT_UPDATE_LOCATION_ATTRIBUTE);
        assertAmountOfInstancesForACategory("storage", storages + 1);

    }

    @Test
    public void verifyResourceCreationFailed_Test() throws Exception {

        // We create a compute instance, that should create also a storage instance.
        // If the creation of the storage fails for some reason, we should be
        // able to see the error in the error attribute of the compute instance.

        String PARENT_UPDATE_LOCATION_ATTRIBUTE = "occi.compute.localstorage";

        Resource resource = ResourcesHandler.factory(
                UUID.randomUUID().toString(),
                COMPUTE_NAME,
                getDefaultResourceAttributes());

        {

            JSONObject create = new JSONObject();
            create.put(UpdaterRequest.CATEGORY_KEY, "WRONG_CATEGORY_NAME");
            create.put(UpdaterRequest.ACTION_KEY, "start");
            create.put(UpdaterRequest.PARENT_UPDATE_LOCATION_KEY, PARENT_UPDATE_LOCATION_ATTRIBUTE);

            // This tasks results response is a resource creation response, a new resource should be created
            Map<String, String> creationTaskResults = new HashMap<String, String>();
            creationTaskResults.put("task1", buildResourceCreationJsonTaskResult(create).toJSONString());
            mockJobResults(scheduler, JOB_ID, new TasksResults(creationTaskResults));
        }

        Reference jobReference = jobReferences.get(JOB_ID);

        Updater updater = new Updater(occiServer, scheduler, UPDATE_PERIOD);
        updater.addResourceToTheUpdateQueue(jobReference, resource);

        assertThereIsOneJobPendingForUpdate(updater);

        waitUntilUpdaterReacts(updater);

        assertAttributeContains(resource, "occi.error.description", "WRONG_CATEGORY_NAME");

    }


    @Test
    public void verifyResourceCreationFailedForOneTaskAndTwoResourcesCreated_Test() throws Exception {

        // Same as before but one task fails and the other one works

        String PARENT_UPDATE_LOCATION_ATTRIBUTE1 = "occi.compute.localstorage";
        String PARENT_UPDATE_LOCATION_ATTRIBUTE2 = "occi.compute.architecture";

        Resource resource = ResourcesHandler.factory(
                UUID.randomUUID().toString(),
                COMPUTE_NAME,
                getDefaultResourceAttributes());

        {
            JSONObject create1 = new JSONObject();
            create1.put(UpdaterRequest.CATEGORY_KEY, "storage");
            create1.put(UpdaterRequest.ACTION_KEY, "start");
            create1.put(UpdaterRequest.PARENT_UPDATE_LOCATION_KEY, PARENT_UPDATE_LOCATION_ATTRIBUTE1);

            JSONObject create2 = new JSONObject();
            create2.put(UpdaterRequest.CATEGORY_KEY, "storage");
            create2.put(UpdaterRequest.ACTION_KEY, "start");
            create2.put(UpdaterRequest.PARENT_UPDATE_LOCATION_KEY, PARENT_UPDATE_LOCATION_ATTRIBUTE2);

            // This tasks results response is a resource creation response, a new resource should be created
            Map<String, String> creationTaskResults = new HashMap<String, String>();
            creationTaskResults.put("task1", buildResourceCreationJsonTaskResult(create1).toJSONString());
            creationTaskResults.put("task2", buildResourceCreationJsonTaskResult(create2).toJSONString());
            creationTaskResults.put("task3", TASK_RESULT_WHEN_ERROR);
            mockJobResults(scheduler, JOB_ID, new TasksResults(creationTaskResults));
        }

        Reference jobReference = jobReferences.get(JOB_ID);

        Integer storages = getAmountOfInstancesForACategory("storage");
        assertAmountOfInstancesForACategory("storage", storages + 0);

        Updater updater = new Updater(occiServer, scheduler, UPDATE_PERIOD);
        updater.addResourceToTheUpdateQueue(jobReference, resource);

        assertThereIsOneJobPendingForUpdate(updater);

        waitUntilUpdaterReacts(updater);

        assertAttributeContains(resource, "occi.error.description", ERROR_STRING);

        assertParentUpdateLocationAttributeIsAValidResourceLocation(resource, PARENT_UPDATE_LOCATION_ATTRIBUTE1);
        assertParentUpdateLocationAttributeIsAValidResourceLocation(resource, PARENT_UPDATE_LOCATION_ATTRIBUTE2);

        assertAmountOfInstancesForACategory("storage", storages + 2);

    }

    private void assertParentUpdateLocationAttributeIsAValidResourceLocation(
            Resource resource,
            String attribute) {
        String value = resource.getAttributes().get(attribute);
        Assert.assertTrue(!value.isEmpty());
        Assert.assertTrue(value.matches(BROKER_URL + ".*?" + UUID_REGEX));
    }

    private void assertAttributeContains(Resource resource, String attribute, String contains) {
        String value = resource.getAttributes().get(attribute);
        Assert.assertTrue(value.contains(contains));
    }

    private int getAmountOfInstancesForACategory(String category) {
        String target = "X-OCCI-Location";
        Response response = occiServer.getAllResources(category);
        String str = response.getEntity().toString();
        Integer occurrencesFromResponse = Utils.countOccurrences(str, target);
        return occurrencesFromResponse;
    }

    private void assertAmountOfInstancesForACategory(String category, int targetAmount) {
        Integer occurrencesFromResponse = getAmountOfInstancesForACategory(category);
        Assert.assertTrue(occurrencesFromResponse == targetAmount);
    }

    private static JSONObject buildResourceCreationJsonTaskResult(JSONObject create) {
        JSONObject task = new JSONObject();
        JSONObject attributes = new JSONObject();
        attributes.put("occi.storage.size", 30);
        attributes.put("provider", "test");
        create.put(UpdaterRequest.ATTRIBUTES_KEY, attributes);
        task.put(UpdaterRequest.CREATE_KEY, create);
        return task;
    }

    // PRIVATE METHODS

    private static Occi createMockOfOcciServer(SchedulerProxy scheduler) throws JAXBException {
        Configuration config = Utils.getConfigurationTest();
        return new OcciServer(config, scheduler);
    }

    private static SchedulerProxy createMockOfSchedulerProxy() throws Exception {
        return mock(SchedulerProxy.class);
    }

    private static JSONObject buildDefaultJsonTaskResult() {
        JSONObject task = new JSONObject();
        task.put("occi.compute.state", "up");
        task.put("occi.compute.hostname", "pepa");
        return task;
    }

    private static JSONObject buildNonDefaultJsonTaskResult() {
        JSONObject task = new JSONObject();
        task.put("occi.compute.cores", "1");
        task.put("occi.compute.memory", "1024");
        return task;
    }


    private static void mockJobResults(SchedulerProxy scheduler,
      String jobId, TasksResults taskResults) throws AuthenticationException, JobNotFinishedException, JobStatusRetrievalException {
        JobIdData jobIdData = new JobIdData();
        jobIdData.setId(Long.parseLong(jobId));
        jobIdData.setReadableName("TestJob" + jobId);
        Reference jobReference = Reference.buildJobReference("", jobIdData);
        jobReferences.put(jobId, jobReference);
        when(scheduler.getAllTaskResults(jobReference.getId())).thenReturn(taskResults);
    }

    private Map<String, String> getDefaultResourceAttributes() {
        return new HashMap<String, String>();
    }

    private void waitUntilUpdaterReacts(Updater updater) {
        int MAX_ATTEMPTS = 100;
        int RETRY_TIME_MSEC = 1 * 100;
        int attempts = 0;
        while (updater.getUpdateQueueSize() != 0) {

            if (attempts++ >= MAX_ATTEMPTS)
                throw new RuntimeException("Updater did not react");

            try {
                Thread.sleep(RETRY_TIME_MSEC);
            } catch (InterruptedException e) {

            }

        }
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
