import org.ow2.proactive.workflowcatalog.cli.ApplicationContext
import org.ow2.proactive.workflowcatalog.cli.ApplicationContextImpl
import org.ow2.proactive.workflowcatalog.cli.CLIException
import org.ow2.proactive.workflowcatalog.cli.cmd.AbstractLoginCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.GetJobResultCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.ListWorkflowsCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.SubmitWorkflowCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.LoginCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.AbstractIModeCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.WcJsHelpCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.LoginWithCredentialsCommand
import org.ow2.proactive.workflowcatalog.cli.cmd.SetUrlCommand

currentContext = ApplicationContextImpl.currentContext()

printWelcomeMsg()

void help() {
    execute(new WcJsHelpCommand())
}

void url(url) {
    execute(new SetUrlCommand('' + url))
}

void login(user) {
    currentContext.setProperty('org.ow2.proactive.workflowcatalog.cli.cmd.AbstractLoginCommand.renewSession', true)
    execute(new LoginCommand('' + user));
}

void loginwithcredentials(pathname) {
    currentContext.setProperty('org.ow2.proactive.workflowcatalog.cli.cmd.AbstractLoginCommand.renewSession', true)
    execute(new LoginWithCredentialsCommand('' + pathname))
}

void getjobresult(jobId) {
    execute(new GetJobResultCommand(jobId))
}

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

void exit() {
	currentContext.setProperty(AbstractIModeCommand.TERMINATE, true)
}

void printWelcomeMsg() {
    print('Loading existing workflows syntax...\r\n');
    // TODO Load workflows
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

