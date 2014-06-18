import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.WorkflowBean
import org.ow2.proactive.workflowcatalog.cli.ApplicationContext
import org.ow2.proactive.workflowcatalog.cli.ApplicationContextImpl
import org.ow2.proactive.workflowcatalog.cli.CLIException
import org.ow2.proactive.workflowcatalog.cli.cmd.AbstractLoginCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.DownloadFileCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.GetJobResultCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.GetJobLogsCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.ListWorkflowsCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.UploadFileCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.SubmitWorkflowCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.LoginCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.AbstractIModeCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.WcJsHelpCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.LoginWithCredentialsCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.SetUrlCommand
import org.ow2.proactive.workflowcatalog.cli.console.JLineDevice
import org.ow2.proactive.workflowcatalog.cli.rest.WorkflowCatalogClient

currentContext = ApplicationContextImpl.currentContext()

printWelcomeMsg()

addAutoComplete("help");
void help() {
    execute(new WcJsHelpCommand())
}

addAutoComplete("USERSPACE");
addAutoComplete("GLOBALSPACE");

addAutoComplete("url http://localhost:8082/workflow-catalog-rest-server");
void url(url) {
    execute(new SetUrlCommand('' + url))
}

addAutoComplete("login admin");
void login(user) {
    currentContext.setProperty('org.ow2.proactive.workflowcatalog.cli.cmd.AbstractLoginCommand.renewSession', true)
    execute(new LoginCommand('' + user));

    updateWorkflowAutoCompletes();
}

void loginwithcredentials(pathname) {
    currentContext.setProperty('org.ow2.proactive.workflowcatalog.cli.cmd.AbstractLoginCommand.renewSession', true)
    execute(new LoginWithCredentialsCommand('' + pathname))
}

addAutoComplete("getjobresult");
addAutoComplete("getjobresult 33");
void getjobresult(jobId) {
    execute(new GetJobResultCommand(jobId + ""))
}

addAutoComplete("listworkflows");
void listworkflows() {
    execute(new ListWorkflowsCommand())
}

addAutoComplete("submitworkflow('workflow-nop.xml', [variable1:\"value1\"], [:])");
void submitworkflow(name, variables, genericInformation) {
    execute(new SubmitWorkflowCommand(name, variables, genericInformation))
}

addAutoComplete("submitworkflow('workflow-nop.xml', [variable1:\"value1\"])");
void submitworkflow(name, variables) {
    submitworkflow(name, variables, [:])
}

addAutoComplete("submitworkflow workflow-nop.xml");
void submitworkflow(name) {
    submitworkflow(name, [:], [:])
}

addAutoComplete("uploadfile /tmp/a.sh");
void uploadfile(srcFilePath) {
    checkFileExists(srcFilePath)
    String dstFileName = getFileName(srcFilePath)
    execute(new UploadFileCommand(srcFilePath, 'USERSPACE', '/', dstFileName))
}

addAutoComplete("uploadfile /tmp/a.sh b.sh");
void uploadfile(srcFilePath, dstFileName) {
    checkFileExists(srcFilePath)
    execute(new UploadFileCommand(srcFilePath, 'USERSPACE', '/', dstFileName))
}

addAutoComplete("uploadfile /tmp/a.sh / b.sh");
void uploadfile(srcFilePath, dstFilePath, dstFileName) {
    checkFileExists(srcFilePath)
    execute(new UploadFileCommand(srcFilePath, 'USERSPACE', dstFilePath, dstFileName))
}

addAutoComplete("uploadfile /tmp/a.sh USERSPACE / b.sh");
void uploadfile(srcFilePath, dstSpaceName, dstFilePath, dstFileName) {
    checkFileExists(srcFilePath)
    execute(new UploadFileCommand(srcFilePath, dstSpaceName, dstFilePath, dstFileName))
}

addAutoComplete("downloadfile /a.sh");
void downloadfile(srcPathName) {
    def dstFileName = getFileName(srcPathName)
    def tempDir = new File(System.getProperty("java.io.tmpdir"))
    def dstFilePath = new File(tempDir, dstFileName).getAbsolutePath()
    execute(new DownloadFileCommand('USERSPACE', srcPathName, dstFilePath))
}

addAutoComplete("downloadfile /a.sh /tmp/a.sh");
void downloadfile(srcPathName, dstFileName) {
    execute(new DownloadFileCommand('USERSPACE', srcPathName, dstFileName))
}

addAutoComplete("downloadfile USERSPACE /a.sh /tmp/a.sh");
void downloadfile(srcSpaceName, srcPathName, dstFileName) {
    execute(new DownloadFileCommand(srcSpaceName, srcPathName, dstFileName))
}

addAutoComplete("getjoblogs");
addAutoComplete("getjoblogs 33");
void getjoblogs(jobId) {
    execute(new GetJobLogsCommand(jobId + ""))
}

addAutoComplete("x date");
addAutoComplete("x hostname");
addAutoComplete("x cat /etc/hosts");
void x(String... cmd) {
    println ">>> Executing command: $cmd"
    println cmd.execute().text
}

void exit() {
	currentContext.setProperty(AbstractIModeCommand.TERMINATE, true)
}

void checkFileExists(srcFilePath) {
    def file = new File(srcFilePath)
    if (!file.exists())
        throw new FileNotFoundException("File $srcFilePath not found")
}

private String getFileName(srcFilePath) {
    return new File(srcFilePath).getName()
}

void printWelcomeMsg() {
    print('Type help() for interactive help \r\n');
     if (getUser(currentContext) == null && getCredFile(currentContext) == null) {
        print('Warning: You are not currently logged in.\r\n')
    }
}

void execute(cmd) {
    def tryAfterReLogin = false
    try {
        cmd.execute(currentContext)
    } catch (Exception e) {
        if (e instanceof CLIException
                && (e.reason() == CLIException.REASON_UNAUTHORIZED_ACCESS)
                && currentContext.getProperty(AbstractLoginCommand.PROP_PERSISTED_SESSION, java.lang.Boolean.TYPE, false)) {
            tryAfterReLogin = true
        } else {
            printError(e)
        }
    }
    if (tryAfterReLogin) {
	currentContext.setProperty(AbstractLoginCommand.PROP_RENEW_SESSION, Boolean.TRUE)
        try {
            if (getCredFile(currentContext) != null) {
                execute(new LoginWithCredentialsCommand(getCredFile(currentContext)))
            } else if (getUser(currentContext) != null) {
                execute(new LoginCommand(getUser(currentContext)))
            }
            cmd.execute(currentContext)
        } catch (e) {
            printError(e)
        }
    }
}

void printError(Exception error) {
    print("An error occurred while executing the command:\r\n")
    if (error != null) {
        error.printStackTrace()
    }
}

def getUser(ApplicationContext context) {
	return context.getProperty(LoginCommand.USERNAME, String.class)
}

def getCredFile(ApplicationContext context) {
	return context.getProperty(LoginWithCredentialsCommand.CRED_FILE, String.class)
}

def addAutoComplete(String command) {
    JLineDevice jline = ((JLineDevice)currentContext.getDevice());
    jline.addAutocompleteCommand(command);
}

def updateWorkflowAutoCompletes() throws CLIException {
    WorkflowCatalogClient client = currentContext.getWorkflowCatalogClient();

    Collection<WorkflowBean> workflows = client.getWorkflowsProxy().getWorkflowList();
    for (WorkflowBean workflow: workflows) {
        addAutocomplete(workflow);
    }
}

def addAutocomplete(WorkflowBean workflow) {
    JLineDevice jline = ((JLineDevice)currentContext.getDevice());
    String submitCmd = generateSubmitCommand(workflow);
    jline.addAutocompleteCommand(submitCmd);
}

private String generateSubmitCommand(WorkflowBean workflow) {
    StringBuilder cmd = new StringBuilder();
    cmd.append("submitworkflow('");
    cmd.append(workflow.name);
    cmd.append("',");
    cmd.append(createGroovyMapCmd(workflow.variables));
    cmd.append(",");
    cmd.append(createGroovyMapCmd(workflow.genericInformation));
    cmd.append(")");
    return cmd.toString();
}

private String createGroovyMapCmd(Map<String, String> map) {
    StringBuilder cmd = new StringBuilder();

    cmd.append("[");
    if (map.size() != 0)
        for (Map.Entry var: map.entrySet()) {
            cmd.append("'");
            cmd.append(var.getKey());
            cmd.append("':'");
            cmd.append(var.getValue());
            cmd.append("',");
        }
    else
        cmd.append(":");

    cmd.append("]");
    return cmd.toString();
}
