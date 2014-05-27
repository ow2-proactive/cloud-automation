package org.ow2.proactive.brokering;

import junit.framework.Assert;
import net.minidev.json.JSONObject;
import org.apache.http.auth.AuthenticationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
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
    private static final String STORAGE_NAME = "storage";

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

    /**
     * A resource is created, and a job is submitted consequently for real stuff.
     * The output of the job's tasks must be used to update the resource's properties. */
    @Test
    public void verifyJsonTaskOutputsAreInsertedIntoResourceProperties_Test() throws Exception {


        Resource resource = ResourcesHandler.factory(
                UUID.randomUUID().toString(),
                COMPUTE_NAME,
                getDefaultComputeResourceAttributes());

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


    /**
     * This tasks results response is a unexpected response, where tasks have results
     * that are non-json formatted. We should find a descriptive error in the
     * error attribute of the resource instance. */
    @Test
    public void verifyOuputJsonTaskOutputIsInsertedIntoResourcePropertiesIfTaskOutputIsNotJson_Test()
            throws Exception {

        Resource resource = ResourcesHandler.factory(
                UUID.randomUUID().toString(),
                COMPUTE_NAME,
                getDefaultComputeResourceAttributes());

        {
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
                getDefaultComputeResourceAttributes());

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
                getDefaultComputeResourceAttributes());

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

        Resource compute = ResourcesHandler.factory(
                UUID.randomUUID().toString(),
                COMPUTE_NAME,
                getDefaultComputeResourceAttributes());

        {

            JSONObject createStorage = new JSONObject();
            createStorage.put(UpdaterRequest.CATEGORY_KEY, STORAGE_NAME);
            createStorage.put(UpdaterRequest.ACTION_KEY, "start");
            createStorage.put(UpdaterRequest.PARENT_UPDATE_LOCATION_KEY, PARENT_UPDATE_LOCATION_ATTRIBUTE);

            // This tasks results response is a resource creation response, a new resource should be created
            Map<String, String> creationTaskResults = new HashMap<String, String>();
            creationTaskResults.put("task1", buildStorageCreationJsonTaskResult(createStorage).toJSONString());
            mockJobResults(scheduler, JOB_ID, new TasksResults(creationTaskResults));

        }

        Reference jobReference = jobReferences.get(JOB_ID);

        Integer storages = getAmountOfInstancesForACategory(STORAGE_NAME);
        assertAmountOfInstancesForACategory(STORAGE_NAME, storages + 0);

        Updater updater = new Updater(occiServer, scheduler, UPDATE_PERIOD);
        updater.addResourceToTheUpdateQueue(jobReference, compute);

        assertThereIsOneJobPendingForUpdate(updater);

        waitUntilUpdaterReacts(updater);
        assertParentUpdateLocationAttributeIsAValidResourceLocation(compute,
                                                                    PARENT_UPDATE_LOCATION_ATTRIBUTE);

        assertAmountOfInstancesForACategory(STORAGE_NAME, storages + 1);

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
                getDefaultComputeResourceAttributes());

        {

            JSONObject create = new JSONObject();
            create.put(UpdaterRequest.CATEGORY_KEY, "WRONG_CATEGORY_NAME");
            create.put(UpdaterRequest.ACTION_KEY, "start");
            create.put(UpdaterRequest.PARENT_UPDATE_LOCATION_KEY, PARENT_UPDATE_LOCATION_ATTRIBUTE);

            // This tasks results response is a resource creation response, a new resource should be created
            Map<String, String> creationTaskResults = new HashMap<String, String>();
            creationTaskResults.put("task1", buildStorageCreationJsonTaskResult(create).toJSONString());
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
                getDefaultComputeResourceAttributes());

        {
            JSONObject create1 = new JSONObject();
            create1.put(UpdaterRequest.CATEGORY_KEY, STORAGE_NAME);
            create1.put(UpdaterRequest.ACTION_KEY, "start");
            create1.put(UpdaterRequest.PARENT_UPDATE_LOCATION_KEY, PARENT_UPDATE_LOCATION_ATTRIBUTE1);

            JSONObject create2 = new JSONObject();
            create2.put(UpdaterRequest.CATEGORY_KEY, STORAGE_NAME);
            create2.put(UpdaterRequest.ACTION_KEY, "start");
            create2.put(UpdaterRequest.PARENT_UPDATE_LOCATION_KEY, PARENT_UPDATE_LOCATION_ATTRIBUTE2);

            // This tasks results response is a 2 resources creation response,
            // so 2 new resources should be created. There is also a failed task
            // and its output should be put in the error attribute of the category instance.
            Map<String, String> creationTaskResults = new HashMap<String, String>();
            creationTaskResults.put("task1", buildStorageCreationJsonTaskResult(create1).toJSONString());
            creationTaskResults.put("task2", buildStorageCreationJsonTaskResult(create2).toJSONString());
            creationTaskResults.put("task3", TASK_RESULT_WHEN_ERROR);
            mockJobResults(scheduler, JOB_ID, new TasksResults(creationTaskResults));

            // This submission is expected when creating the new resource
            when(scheduler.submitJob((File)notNull())).thenReturn(new JobIdData());
        }

        Reference jobReference = jobReferences.get(JOB_ID);

        Integer storages = getAmountOfInstancesForACategory(STORAGE_NAME);
        assertAmountOfInstancesForACategory(STORAGE_NAME, storages + 0);

        Updater updater = new Updater(occiServer, scheduler, UPDATE_PERIOD);
        updater.addResourceToTheUpdateQueue(jobReference, resource);

        assertThereIsOneJobPendingForUpdate(updater);

        waitUntilUpdaterReacts(updater);

        assertParentUpdateLocationAttributeIsAValidResourceLocation(resource, PARENT_UPDATE_LOCATION_ATTRIBUTE1);
        assertParentUpdateLocationAttributeIsAValidResourceLocation(resource, PARENT_UPDATE_LOCATION_ATTRIBUTE2);

        assertAmountOfInstancesForACategory(STORAGE_NAME, storages + 2);

    }


    @Test
    public void verifyResourceUpdate_Test() throws Exception {

        // A compute instance already exists.
        // Then we create a storage and we attach it to the compute.
        // So there should be
        // - compute(occi.compute.localstorage) attached to storage
        // - storage(occi.core.target) attached to compute

        Resource compute = ResourcesHandler.factory(
                UUID.randomUUID().toString(),
                COMPUTE_NAME,
                getDefaultComputeResourceAttributes());

        Resource storage = ResourcesHandler.factory(
                UUID.randomUUID().toString(),
                STORAGE_NAME,
                getDefaultStorageResourceAttributes());

        {

            JSONObject task = new JSONObject();
            // These two will update the storage instance
            task.put("occi.storage.size", 30);
            task.put("occi.core.target", compute.getUrl().toString());
            JSONObject update = new JSONObject();
            JSONObject attributes = new JSONObject();
            attributes.put("occi.compute.localstorage", UpdaterRequest.LOCATION_OF_PARENT_KEY);

            update.put(UpdaterRequest.CATEGORY_KEY, COMPUTE_NAME);
            update.put(UpdaterRequest.ATTRIBUTES_KEY, attributes);
            update.put(UpdaterRequest.ID_KEY, compute.getUuid());
            update.put(UpdaterRequest.ACTION_KEY, "update");

            task.put(UpdaterRequest.UPDATE_KEY, update);

            Map<String, String> creationTaskResults = new HashMap<String, String>();
            creationTaskResults.put("task1", task.toJSONString());
            mockJobResults(scheduler, JOB_ID, new TasksResults(creationTaskResults));

            // This submission is expected when creating the new resource
            when(scheduler.submitJob((File)notNull())).thenReturn(new JobIdData());
        }

        Reference jobReference = jobReferences.get(JOB_ID);

        Updater updater = new Updater(occiServer, scheduler, UPDATE_PERIOD);
        updater.addResourceToTheUpdateQueue(jobReference, storage);

        assertThereIsOneJobPendingForUpdate(updater);

        waitUntilUpdaterReacts(updater);

        String error = storage.getAttributes().get("occi.error.description");
        Assert.assertTrue(error, error.isEmpty());
        Assert.assertTrue(storage.getAttributes().get("occi.storage.size").equals("30"));
        Assert.assertTrue(storage.getAttributes().get("occi.core.target").equals(compute.getUrl().toString()));

        Assert.assertTrue(compute.getAttributes().get("occi.compute.localstorage").equals(storage.getUrl().toString()));

    }


    // PRIVATE METHODS

    private void assertParentUpdateLocationAttributeIsAValidResourceLocation(
            Resource resource,
            String attribute) {
        String value = resource.getAttributes().get(attribute);
        String error  = resource.getAttributes().get("occi.error.description");

        Assert.assertTrue("Value is empty: " + error, !value.isEmpty());
        Assert.assertTrue("Value is not a location: " + value + "-" + error,
                          value.matches(BROKER_URL + ".*?" + UUID_REGEX));
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

    private static JSONObject buildStorageCreationJsonTaskResult(JSONObject create) {
        JSONObject task = new JSONObject();
        JSONObject attributes = new JSONObject();
        attributes.put("occi.storage.size", 30);
        attributes.put("occi.storage.target", UpdaterRequest.LOCATION_OF_PARENT_KEY);
        attributes.put("provider", "test");
        create.put(UpdaterRequest.ATTRIBUTES_KEY, attributes);
        task.put(UpdaterRequest.CREATE_KEY, create);
        return task;
    }

    private static Occi createMockOfOcciServer(SchedulerProxy scheduler) throws JAXBException {
        Configuration config = Utils.getConfigurationTest();
        return new OcciServer(config, scheduler);
    }

    private static SchedulerProxy createMockOfSchedulerProxy() throws Exception {
        SchedulerProxy scheduler =  mock(SchedulerProxy.class);
        when(scheduler.submitJob((File)notNull())).thenReturn(new JobIdData());
        return scheduler;
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

    private Map<String, String> getDefaultComputeResourceAttributes() {
        return new HashMap<String, String>();
    }

    private Map<String, String> getDefaultStorageResourceAttributes() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("occi.storage.size", "10");
        return map;
    }

    private void waitUntilUpdaterReacts(Updater updater) throws InterruptedException {
        int MAX_ATTEMPTS = 100;
        int RETRY_TIME_MSEC = 1 * 100;
        int attempts = 0;
        while (updater.getUpdateQueueSize() != 0) {
            if (attempts++ >= MAX_ATTEMPTS)
                throw new RuntimeException("Updater did not react");
            Thread.sleep(RETRY_TIME_MSEC);
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
