package org.ow2.proactive.brokering;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.ow2.proactive.brokering.occi.OcciServer;
import org.ow2.proactive.brokering.occi.Resource;
import org.ow2.proactive.brokering.occi.ResourceBuilder;
import org.ow2.proactive.brokering.occi.categories.Utils;
import org.ow2.proactive.brokering.occi.client.ActionTriggerHandler;
import org.ow2.proactive.brokering.occi.database.DatabaseFactory;
import org.ow2.proactive.brokering.occi.database.InMemoryDB;
import org.ow2.proactive.brokering.updater.Updater;
import org.ow2.proactive.brokering.updater.requests.UpdaterRequest;
import org.ow2.proactive.workflowcatalog.Catalog;
import org.ow2.proactive.workflowcatalog.Reference;
import org.ow2.proactive.workflowcatalog.utils.scheduling.ISchedulerProxy;
import org.ow2.proactive.workflowcatalog.utils.scheduling.JobNotFinishedException;
import org.ow2.proactive.workflowcatalog.utils.scheduling.JobStatusRetrievalException;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerProxy;
import org.ow2.proactive.workflowcatalog.utils.scheduling.TasksResults;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import junit.framework.Assert;
import net.minidev.json.JSONObject;
import org.apache.http.auth.AuthenticationException;
import org.junit.Before;
import org.junit.Test;

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
    private int jobId;

    private OcciServer occiServer;
    private SchedulerProxy scheduler;
    private Map<String, Reference> jobReferences = new HashMap<String, Reference>();
    private Updater updater;
    private InMemoryDB database;

    @Before
    public void setUp() throws Exception {
        jobId = 1;
        scheduler = createMockOfSchedulerProxy();
        database = new InMemoryDB();
        occiServer = createMockOfOcciServer(scheduler, database);
        updater = new Updater(occiServer, scheduler, UPDATE_PERIOD, BROKER_URL);
        occiServer.setUpdater(updater);
    }

    /**
     * A resource is created, and a job is submitted consequently for real stuff.
     * The output of the job's tasks must be used to update the resource's properties. */
    @Test
    public void verifyJsonTaskOutputsAreInsertedIntoResourceProperties_Test() throws Exception {


        Resource resource = ResourceBuilder.factory(
          UUID.randomUUID().toString(),
          COMPUTE_NAME,
          getDefaultComputeResourceAttributes());
        database.store(resource);

        {
            // This tasks results response is a expected response, where tasks have results
            // that are json formatted.
            Map<String, String> goodTaskResults = new HashMap<String, String>();
            goodTaskResults.put("task1", buildDefaultJsonTaskResult().toJSONString());
            goodTaskResults.put("task2", buildNonDefaultJsonTaskResult().toJSONString());
            mockJobResults(scheduler, JOB_ID, new TasksResults(goodTaskResults));
        }
        Reference jobReference = jobReferences.get(JOB_ID);

        updater.addResourceToTheUpdateQueue(jobReference, resource);

        waitUntilUpdateProcessedEverything(updater);

        assertResourceAttributesAreCorrectlyTakenFromTaskResults(resource);

    }


    /**
     * This tasks results response is a unexpected response, where tasks have results
     * that are non-json formatted. We should find a descriptive error in the
     * error attribute of the resource instance. */
    @Test
    public void verifyOuputJsonTaskOutputIsInsertedIntoResourcePropertiesIfTaskOutputIsNotJson_Test()
            throws Exception {

        Resource resource = ResourceBuilder.factory(
          UUID.randomUUID().toString(),
          COMPUTE_NAME,
          getDefaultComputeResourceAttributes());
        database.store(resource);

        {
            Map<String, String> badTaskResults = new HashMap<String, String>();
            badTaskResults.put("task1", "nonJsonTaskResult1");
            badTaskResults.put("task2", "nonJsonTaskResult2");
            mockJobResults(scheduler, JOB_ID, new TasksResults(badTaskResults));
        }
        Reference jobReference = jobReferences.get(JOB_ID);

        updater.addResourceToTheUpdateQueue(jobReference, resource);

        waitUntilUpdateProcessedEverything(updater);

    }

    @Test
    public void verifyAllOuputJsonTaskOutputAreInsertedIntoResourcePropertiesEvenIfNotAllTaskOutputsAreJson_Test()
            throws Exception {

        Resource resource = ResourceBuilder.factory(
          UUID.randomUUID().toString(),
          COMPUTE_NAME,
          getDefaultComputeResourceAttributes());
        database.store(resource);

        {
            // This tasks results response is a sort of expected response, where some tasks have results
            // that are json formatted and some do not.
            Map<String, String> mixedTaskResults = new HashMap<String, String>();
            mixedTaskResults.put("task1", buildDefaultJsonTaskResult().toJSONString());
            mixedTaskResults.put("task2", "nonJsonTaskResult2");
            mockJobResults(scheduler, JOB_ID, new TasksResults(mixedTaskResults));
        }

        Reference jobReference = jobReferences.get(JOB_ID);

        updater.addResourceToTheUpdateQueue(jobReference, resource);

        waitUntilUpdateProcessedEverything(updater);

        assertSomeResourceAttributesAreCorrectlyTakenFromTaskResults(resource);

    }

    @Test
    public void verifyResourcePropertiesAreUpdatedIfJobError_Test()
            throws Exception {

        Resource resource = ResourceBuilder.factory(
          UUID.randomUUID().toString(),
          COMPUTE_NAME,
          getDefaultComputeResourceAttributes());
        database.store(resource);

        {
            // This tasks results response is a non expected response, where some tasks fail.
            Map<String, String> failedTaskResults = new HashMap<String, String>();
            failedTaskResults.put("task1", buildDefaultJsonTaskResult().toJSONString());
            failedTaskResults.put("task2", "nonJsonTaskResult2");
            failedTaskResults.put("task3", TASK_RESULT_WHEN_ERROR);
            mockJobResults(scheduler, JOB_ID, new TasksResults(failedTaskResults));
        }

        Reference jobReference = jobReferences.get(JOB_ID);

        updater.addResourceToTheUpdateQueue(jobReference, resource);

        waitUntilUpdateProcessedEverything(updater);

        assertSomeResourceAttributesAreCorrectlyTakenFromTaskResults(resource);
        assertResourceAttributesErrorDescriptionContains(resource, ERROR_STRING);

    }

    @Test
    public void verifyResourceCreation_Test() throws Exception {

        // We create a compute instance, that should create also a storage instance.
        // The location url of the storage instance should be put in the
        // PARENT_UPDATE_LOCATION_ATTRIBUTE of the compute resource.

        String PARENT_UPDATE_LOCATION_ATTRIBUTE = "occi.compute.localstorage";

        Resource compute = ResourceBuilder.factory(
          UUID.randomUUID().toString(),
          COMPUTE_NAME,
          getDefaultComputeResourceAttributes());
        database.store(compute);

        {

            JSONObject createStorage = new JSONObject();
            createStorage.put(UpdaterRequest.CATEGORY_KEY, STORAGE_NAME);
            createStorage.put(UpdaterRequest.ACTION_KEY, "start");
            createStorage.put(UpdaterRequest.PARENT_UPDATE_LOCATION_KEY, PARENT_UPDATE_LOCATION_ATTRIBUTE);

            // This tasks results response is a resource creation response, a new resource should be created
            Map<String, String> creationTaskResults = new HashMap<String, String>();
            creationTaskResults.put("task1", buildStorageCreationJsonTaskResult(createStorage).toJSONString());
            mockJobResults(scheduler, "0", new TasksResults(creationTaskResults));

            Map<String, String> storageCreationTaskResults = new HashMap<String, String>();
            storageCreationTaskResults.put("task1", "{}");
            mockJobResults(scheduler, "1", new TasksResults(storageCreationTaskResults));

        }

        Reference jobReference = jobReferences.get(JOB_ID);

        Integer storages = getAmountOfInstancesForACategory(STORAGE_NAME);
        assertAmountOfInstancesForACategory(STORAGE_NAME, storages + 0);

        updater.addResourceToTheUpdateQueue(jobReference, compute);

        waitUntilUpdateProcessedEverything(updater);
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

        Resource resource = ResourceBuilder.factory(
          UUID.randomUUID().toString(),
          COMPUTE_NAME,
          getDefaultComputeResourceAttributes());
        database.store(resource);

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

        updater.addResourceToTheUpdateQueue(jobReference, resource);

        waitUntilUpdateProcessedEverything(updater);

        assertAttributeContains(resource, "occi.error.description", "WRONG_CATEGORY_NAME");

    }


    @Test
    public void verifyResourceCreationFailedForOneTaskAndTwoResourcesCreated_Test() throws Exception {

        // Same as before but one task fails and the other one works

        String PARENT_UPDATE_LOCATION_ATTRIBUTE1 = "occi.compute.localstorage";
        String PARENT_UPDATE_LOCATION_ATTRIBUTE2 = "occi.compute.architecture";

        Resource resource = ResourceBuilder.factory(
          UUID.randomUUID().toString(),
          COMPUTE_NAME,
          getDefaultComputeResourceAttributes());
        database.store(resource);

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
        }

        Reference jobReference = jobReferences.get(JOB_ID);

        Integer storages = getAmountOfInstancesForACategory(STORAGE_NAME);
        assertAmountOfInstancesForACategory(STORAGE_NAME, storages + 0);

        updater.addResourceToTheUpdateQueue(jobReference, resource);

        waitUntilUpdateProcessedEverything(updater);

        assertParentUpdateLocationAttributeIsAValidResourceLocation(resource,
          PARENT_UPDATE_LOCATION_ATTRIBUTE1);
        assertParentUpdateLocationAttributeIsAValidResourceLocation(resource,
          PARENT_UPDATE_LOCATION_ATTRIBUTE2);

        assertAmountOfInstancesForACategory(STORAGE_NAME, storages + 2);

    }


    @Test
    public void verifyResourceUpdate_Test() throws Exception {

        // A compute instance already exists.
        // Then we create a storage and we attach it to the compute.
        // So there should be
        // - compute(occi.compute.localstorage) attached to storage
        // - storage(occi.core.target) attached to compute

        Resource compute = ResourceBuilder.factory(
          UUID.randomUUID().toString(),
          COMPUTE_NAME,
          getDefaultComputeResourceAttributes());
        database.store(compute);

        Resource storage = ResourceBuilder.factory(
          UUID.randomUUID().toString(),
          STORAGE_NAME,
          getDefaultStorageResourceAttributes());
        database.store(storage);

        {

            JSONObject task = new JSONObject();
            // These two will update the storage instance
            task.put("occi.storage.size", 30);
            task.put("occi.core.target", compute.getFullPath(BROKER_URL));
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

        updater.addResourceToTheUpdateQueue(jobReference, storage);

        waitUntilUpdateProcessedEverything(updater);

        String error = storage.getAttributes().get("occi.error.description");
        Assert.assertTrue(error, error.isEmpty());
        Assert.assertEquals("30", storage.getAttributes().get("occi.storage.size"));
        Assert.assertEquals(compute.getFullPath(BROKER_URL), storage.getAttributes().get("occi.core.target"));
        Assert.assertEquals(storage.getFullPath(BROKER_URL), compute.getAttributes().get("occi.compute.localstorage"));
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
        return Utils.countOccurrences(str, target);
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

    private static OcciServer createMockOfOcciServer(final SchedulerProxy scheduler, InMemoryDB db) throws JAXBException {
        DatabaseFactory mockedDatabaseFactory = mock(DatabaseFactory.class);
        when(mockedDatabaseFactory.build()).thenReturn(db);
        File catalogPath = Utils.getScriptsPath("config/catalog/", "/config/catalog");
        Catalog catalog = new Catalog(catalogPath, 10);
        Rules rules = mock(Rules.class);
        Broker broker = new Broker(catalog, rules, new SchedulerFactory() {
            @Override
            public ISchedulerProxy getScheduler() {
                return scheduler;
            }
        }, new ActionTriggerHandler("config/actions",10,"config/conditions/",10));
        return new OcciServer(broker, null, mockedDatabaseFactory, BROKER_URL);
    }

    private SchedulerProxy createMockOfSchedulerProxy() throws Exception {
        SchedulerProxy scheduler =  mock(SchedulerProxy.class);
        JobIdData jobIdData = new JobIdData();
        jobIdData.setId(jobId++);
        when(scheduler.submitJob((File)notNull())).thenReturn(jobIdData);
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


    private void mockJobResults(SchedulerProxy scheduler,
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

    private void waitUntilUpdateProcessedEverything(Updater updater) throws InterruptedException {
        int MAX_ATTEMPTS = 1000;
        int RETRY_TIME_MSEC = 10;
        int attempts = 0;
        while (updater.getUpdateQueueSize() != 0) {
            if (attempts++ >= MAX_ATTEMPTS)
                throw new RuntimeException("Updater did not react");
            Thread.sleep(RETRY_TIME_MSEC);
        }
    }

    private void assertResourceAttributesAreCorrectlyTakenFromTaskResults(Resource resource) {
        Assert.assertEquals(resource.getAttributes().get("occi.compute.state"), "up");
        Assert.assertEquals(resource.getAttributes().get("occi.compute.cores"), "1");
        Assert.assertEquals(resource.getAttributes().get("occi.compute.memory"), "1024");
        Assert.assertEquals(resource.getAttributes().get("occi.compute.hostname"), "pepa");
    }

    private void assertSomeResourceAttributesAreCorrectlyTakenFromTaskResults(Resource resource) {
        Assert.assertEquals(resource.getAttributes().get("occi.compute.state"), "up");
        Assert.assertEquals(resource.getAttributes().get("occi.compute.hostname"), "pepa");
    }

    private void assertResourceAttributesErrorDescriptionContains(Resource resource, String message) {
        Assert.assertTrue(resource.getAttributes().get("occi.error.description").contains(message));
    }

}
