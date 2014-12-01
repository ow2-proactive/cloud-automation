import jline.ArgumentCompletor
import jline.Completor
import jline.FileNameCompletor
import jline.NullCompletor
import jline.SimpleCompletor
import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.WorkflowBean
import org.ow2.proactive.workflowcatalog.cli.ApplicationContext
import org.ow2.proactive.workflowcatalog.cli.ApplicationContextImpl
import org.ow2.proactive.workflowcatalog.cli.CLIException
import org.ow2.proactive.workflowcatalog.cli.cmd.AbstractLoginCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.DownloadFileCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.GetJobResultCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.GetJobLogsCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.ListWorkflowsCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.LogoutCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.UploadFileCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.SubmitWorkflowCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.LoginCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.AbstractIModeCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.WcJsHelpCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.LoginWithCredentialsCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.SetUrlCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.SetInsecureAccessCommand
import org.ow2.proactive.workflowcatalog.cli.console.JLineDevice
import org.ow2.proactive.workflowcatalog.cli.rest.WorkflowCatalogClient

currentContext = ApplicationContextImpl.currentContext()

printWelcomeMsg()

addAutoComplete(new ArgumentCompletor(new SimpleCompletor("help"), new NullCompletor()));
void help() {
    execute(new WcJsHelpCommand())
}

addAutoComplete(new ArgumentCompletor(new SimpleCompletor("url"), new SimpleCompletor("http://localhost:8082/workflow-catalog-rest-server"), new NullCompletor()));
void url(url) {
    execute(new SetUrlCommand('' + url))
}

addAutoComplete(new ArgumentCompletor(new SimpleCompletor("insecure"), new NullCompletor()));
void insecure() {
    execute(new SetInsecureAccessCommand())
}

addAutoComplete(new ArgumentCompletor(new SimpleCompletor("login"), new SimpleCompletor(["admin", "user", "demo"] as String[]), new NullCompletor()));
void login(user) {
    currentContext.setProperty('org.ow2.proactive.workflowcatalog.cli.cmd.AbstractLoginCommand.renewSession', true)
    execute(new LoginCommand('' + user));

    updateWorkflowAutoCompletes();
}

addAutoComplete(new ArgumentCompletor(new SimpleCompletor("logout"), new NullCompletor()));
void logout() {
    execute(new LogoutCommand());
}

addAutoComplete(new ArgumentCompletor(new SimpleCompletor("login"), new SimpleCompletor(["admin", "user", "demo"] as String[]), new FileNameCompletor(), new NullCompletor()));
void login(username, pathname) {
    currentContext.setProperty('org.ow2.proactive.workflowcatalog.cli.cmd.AbstractLoginCommand.renewSession', true)
    execute(new LoginWithCredentialsCommand(username + "", pathname + ""))

    updateWorkflowAutoCompletes();
}

addAutoComplete(new ArgumentCompletor(new SimpleCompletor("getjobresult"), new SimpleCompletor(["0", "1"] as String[]), new NullCompletor()));
void getjobresult(jobId) {
    execute(new GetJobResultCommand(jobId + ""))
}

addAutoComplete(new ArgumentCompletor(new SimpleCompletor("listworkflows"), new NullCompletor()));
void listworkflows() {
    execute(new ListWorkflowsCommand())
}

void submitworkflow(name, variables, genericInformation) {
    execute(new SubmitWorkflowCommand(name, variables, genericInformation))
}

void submitworkflow(name, variables) {
    submitworkflow(name, variables, [:])
}

void submitworkflow(name) {
    submitworkflow(name, [:], [:])
}

addAutoComplete(new ArgumentCompletor(new SimpleCompletor("uploadfile"), new FileNameCompletor(), new NullCompletor()));
void uploadfile(srcFilePath) {
    checkFileExists(srcFilePath)
    String dstFileName = getFileName(srcFilePath)
    execute(new UploadFileCommand(srcFilePath, 'USERSPACE', '/', dstFileName))
}

addAutoComplete(new ArgumentCompletor(new SimpleCompletor("uploadfile"), new FileNameCompletor(), new SimpleCompletor("destination"), new NullCompletor()));
void uploadfile(srcFilePath, dstFileName) {
    checkFileExists(srcFilePath)
    execute(new UploadFileCommand(srcFilePath, 'USERSPACE', '/', dstFileName))
}

addAutoComplete(new ArgumentCompletor(new SimpleCompletor("uploadfile"), new FileNameCompletor(), new SimpleCompletor("/"), new SimpleCompletor("destination"), new NullCompletor()));
void uploadfile(srcFilePath, dstFilePath, dstFileName) {
    checkFileExists(srcFilePath)
    execute(new UploadFileCommand(srcFilePath, 'USERSPACE', dstFilePath, dstFileName))
}

addAutoComplete(new ArgumentCompletor(new SimpleCompletor("uploadfile"), new SimpleCompletor(["USERSPACE", "GLOBALSPACE"] as String[]), new SimpleCompletor("/"), new SimpleCompletor("destination"), new NullCompletor()));
void uploadfile(srcFilePath, dstSpaceName, dstFilePath, dstFileName) {
    checkFileExists(srcFilePath)
    execute(new UploadFileCommand(srcFilePath, dstSpaceName, dstFilePath, dstFileName))
}

addAutoComplete(new ArgumentCompletor(new SimpleCompletor("downloadfile"), new SimpleCompletor("/remotefile.txt"), new NullCompletor()));
void downloadfile(srcPathName) {
    def dstFileName = getFileName(srcPathName)
    def tempDir = new File(System.getProperty("java.io.tmpdir"))
    def dstFilePath = new File(tempDir, dstFileName).getAbsolutePath()
    execute(new DownloadFileCommand('USERSPACE', srcPathName, dstFilePath))
}

addAutoComplete(new ArgumentCompletor(new SimpleCompletor("downloadfile"), new SimpleCompletor("/remotefile.txt"), new FileNameCompletor(), new NullCompletor()));
void downloadfile(srcPathName, dstFileName) {
    execute(new DownloadFileCommand('USERSPACE', srcPathName, dstFileName))
}

addAutoComplete(new ArgumentCompletor(new SimpleCompletor("downloadfile"), new SimpleCompletor(["USERSPACE", "GLOBALSPACE"] as String[]), new SimpleCompletor("/remotefile.txt"), new FileNameCompletor(), new NullCompletor()));
void downloadfile(srcSpaceName, srcPathName, dstFileName) {
    execute(new DownloadFileCommand(srcSpaceName, srcPathName, dstFileName))
}

addAutoComplete(new ArgumentCompletor(new SimpleCompletor("getjoblogs"), new SimpleCompletor(["0", "1"] as String[]), new NullCompletor()));
void getjoblogs(jobId) {
    execute(new GetJobLogsCommand(jobId + ""))
}

addAutoComplete(new ArgumentCompletor(new SimpleCompletor("x"), new SimpleCompletor(["date", "hostname", "ls /tmp"] as String[]), new FileNameCompletor()));
void x(String... cmd) {
    println ">>> Executing command: $cmd"
    println cmd.execute().text
}

addAutoComplete(new ArgumentCompletor(new SimpleCompletor("exit")));
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

def addAutoComplete(Completor completor) {
    JLineDevice jline = ((JLineDevice)currentContext.getDevice());
    jline.addAutocompleteCommand(completor);
}

addAutoComplete(new ArgumentCompletor(new SimpleCompletor("updateWorkflowAutoCompletes")));
def updateWorkflowAutoCompletes() {
    WorkflowCatalogClient client = currentContext.getWorkflowCatalogClient();

    Collection<WorkflowBean> workflows = client.getWorkflowsProxy().getWorkflowList();
    for (WorkflowBean workflow: workflows) {
        addAutocompleteWorkflow(workflow);
    }
}

def addAutocompleteWorkflow(WorkflowBean workflow) {
    addAutoComplete(generateSubmitCommand(workflow));
    addAutoComplete(generateSubmitCommandBasic(workflow));
    addAutoComplete(generateSubmitCommandExtended(workflow));
}

private Completor generateSubmitCommand(WorkflowBean workflow) {
    return new ArgumentCompletor(
            new SimpleCompletor("submitworkflow"),
            new SimpleCompletor("("),
            new SimpleCompletor("'"+workflow.name+"'"),
            new SimpleCompletor(")"),
            new NullCompletor()
    )
}

private Completor generateSubmitCommandBasic(WorkflowBean workflow) {
    return new ArgumentCompletor(
            new SimpleCompletor("submitworkflow"),
            new SimpleCompletor("("),
            new SimpleCompletor("'"+workflow.name+"'"),
            new SimpleCompletor(","),
            new SimpleCompletor(createGroovyMapCmd(workflow.variables)),
            new SimpleCompletor(")"),
            new NullCompletor()
    )
}

private Completor generateSubmitCommandExtended(WorkflowBean workflow) {
    return new ArgumentCompletor(
            new SimpleCompletor("submitworkflow"),
            new SimpleCompletor("("),
            new SimpleCompletor("'"+workflow.name+"'"),
            new SimpleCompletor(","),
            new SimpleCompletor(createGroovyMapCmd(workflow.variables)),
            new SimpleCompletor(","),
            new SimpleCompletor(createGroovyMapCmd(workflow.genericInformation)),
            new SimpleCompletor(")"),
            new NullCompletor()
    )
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
