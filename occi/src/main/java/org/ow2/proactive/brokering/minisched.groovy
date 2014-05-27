package org.ow2.proactive.brokering

import org.ow2.proactive.workflowcatalog.utils.scheduling.*
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.Executors

def jobFile = new File('/home/ybonnaffe/src/brokering/occi/src/main/resources/config/catalog-test/test-create.xml')
def job = new XmlSlurper().parse(jobFile)

def scheduler = new MiniScheduler()
jobId = scheduler.submitJob(jobFile)
println scheduler.getAllTaskResults(jobId.id as String)

class MiniScheduler implements ISchedulerProxy {
    long ids = 1
    Map<String, Map<String, String>> jobResults = new ConcurrentHashMap<>()
    Executor workers = Executors.newWorkStealingPool()

    @Override
    TasksResults getAllTaskResults(String jobId) throws JobNotFinishedException, JobStatusRetrievalException {
        if(!jobResults[jobId]){
            throw new JobNotFinishedException("Not finished")
        }
        return new TasksResults(jobResults[jobId])
    }

    @Override
    JobIdData submitJob(File jobFile) throws JobSubmissionException {
        def job = new XmlSlurper().parse(jobFile)

        def jobId = new JobIdData(id: ids++, readableName: job.@name)

        workers.execute({
            println "Running $jobId.id $jobId.readableName"

            def taskResults = [:]
            jobResults.put(jobId.id as String, taskResults)

            job.taskFlow.task.each { task ->
                println "--- Running task ${task.@name} ---"
                String code = task.scriptExecutable.script.code

                def shell = new GroovyShell()
                shell.evaluate(code)
                def taskResult = shell.getVariable("result")
                println "--- ---"

                taskResults.put(task.@name.text(), taskResult as String)
            }
        })

        return jobId
    }

    @Override
    void disconnectFromScheduler() {

    }

    @Override
    String getSessionId() {
        return 42
    }
}