package org.ow2.proactive.brokering
import groovy.util.logging.Log4j
import org.ow2.proactive.workflowcatalog.SchedulerAuthentication
import org.ow2.proactive.workflowcatalog.utils.scheduling.*
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException

import javax.security.auth.login.LoginException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.Executors

def jobFile = new File('/home/ybonnaffe/src/brokering/occi/src/main/resources/config/catalog-test/compute-create.xml')

def scheduler = MiniScheduler.instance
jobId = scheduler.submitJob(jobFile)
Thread.sleep(6000)
println scheduler.getAllTaskResults(jobId.id as String)

@Log4j
class MiniScheduler implements ISchedulerProxy {
    public static MiniScheduler instance = new MiniScheduler()

    long ids = 1
    Map<String, Map<String, String>> jobResults = new ConcurrentHashMap<>()
    Executor workers = Executors.newCachedThreadPool()

    @Override
    TasksResults getAllTaskResults(String jobId) throws JobNotFinishedException, JobStatusRetrievalException {
        if (!jobResults[jobId]) {
            throw new JobNotFinishedException("Not finished")
        }
        return new TasksResults(jobResults[jobId])
    }

    @Override
    JobIdData submitJob(File jobFile) throws JobSubmissionException {
        def job = new XmlSlurper().parse(jobFile)

        def jobId = new JobIdData(id: ids++, readableName: job.@name)

        workers.execute({
            log.debug "Running $jobId.id $jobId.readableName"

            def taskResults = [:]

            def variables = [:]
            job.variables.variable.each { variable ->
                variables << [(variable.@name as String): (variable.@value as String)]
            }
            job.taskFlow.task.each { task ->
                log.debug "--- Running task ${task.@name} ---"
                String code = task.scriptExecutable.script.code

                def shell = new GroovyShell()
                shell.setVariable("variables", variables)
                shell.evaluate(code)
                def taskResult = shell.getVariable("result")
                log.debug "--- Done ($taskResult) ---"

                taskResults.put(task.@name.text(), taskResult as String)
            }
            jobResults.put(jobId.id as String, taskResults)
        })

        return jobId
    }

    @Override
    List<org.ow2.proactive_grid_cloud_portal.studio.Workflow> listWorkflows() throws WorkflowsRetrievalException {
        return new ArrayList<org.ow2.proactive_grid_cloud_portal.studio.Workflow>();
    }

    @Override
    void disconnectFromScheduler() {

    }

    @Override
    String getSessionId() {
        return 42
    }

    public static class Authentication extends SchedulerAuthentication {
        @Override
        protected ISchedulerProxy loginToSchedulerRestApi(SchedulerLoginData login) throws LoginException, SchedulerRestException {
            return instance
        }
    }
}