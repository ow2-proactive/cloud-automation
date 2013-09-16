package org.ow2.proactive.brokering;

import junit.framework.Assert;
import org.apache.http.auth.AuthenticationException;
import org.junit.Test;
import org.ow2.proactive.brokering.occi.Database;
import org.ow2.proactive.brokering.occi.OcciServer;
import org.ow2.proactive.brokering.occi.Resource;
import org.ow2.proactive.brokering.occi.api.Occi;
import org.ow2.proactive.brokering.occi.infrastructure.Utils;
import org.ow2.proactive.brokering.utils.scheduling.SchedulerProxy;

import javax.json.JsonObject;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class UpdaterTest {

    private static final String HOST = "host";
    private static final String CATEGORY_COMPUTE = "compute";
    private static final UUID UUID_VALUE = UUID.randomUUID();

    private static List<Reference> jobReferences = new ArrayList<Reference>();

    @Test
    public void verifyJsonTaskOutputsAreInsertedIntoResourceProperties_Test() throws Exception {
        Occi occi = getMockOfOcciServer();
        SchedulerProxy scheduler = getMockOfSchedulerProxy();
        Reference jobReference = getJobResultWithExpectedTasksOutput();
        Resource resource1 = Resource.factory(UUID_VALUE, HOST, CATEGORY_COMPUTE, getDefaultComputeAttributes());

        Updater updater = new Updater(occi, scheduler, TimeUnit.MILLISECONDS.toMillis(1));
        updater.addResourceToTheUpdateQueue(jobReference, resource1);

        Thread.sleep(100); // let updates take place after job if finished

        // assert that resource1 actually contains properties that are result of the tasks of the job

        Assert.assertTrue(resource1.getAttributes().get("occi.compute.state").equalsIgnoreCase("up"));
        Assert.assertTrue(resource1.getAttributes().get("occi.compute.cores").equalsIgnoreCase("1"));
        Assert.assertTrue(resource1.getAttributes().get("occi.compute.memory").equalsIgnoreCase("1024"));
        Assert.assertTrue(resource1.getAttributes().get("occi.compute.hostname").equalsIgnoreCase("pepa"));
    }

    private Reference getJobResultWithExpectedTasksOutput() {
        return jobReferences.get(0);
    }

    private Occi getMockOfOcciServer() {
        OcciServer.setDatabase(mock(Database.class));
        new OcciServer();
        return OcciServer.getInstance();

    }

    private SchedulerProxy getMockOfSchedulerProxy() throws IOException, AuthenticationException {
        SchedulerProxy scheduler = mock(SchedulerProxy.class);

        Properties restResponses = new Properties();
        restResponses.load(UpdaterTest.class.getResourceAsStream("updater.properties"));

        for (int i=1; i<=3; i++) {
            Reference jobReference = Reference.buildJobReference(true, TestUtils.createSubmitResponse(i, "TestJob" + i));
            jobReferences.add(jobReference);
            JsonObject taskRes = Utils.convertToJson(restResponses.get(i + "").toString());
            when(scheduler.getAllTaskResultsAsJson(jobReference)).thenReturn(taskRes);
        }
        return scheduler;
    }

    private Map<String,String> getDefaultComputeAttributes() {
        Map<String, String> map = new HashMap<String, String>();
        return map;
    }

}
