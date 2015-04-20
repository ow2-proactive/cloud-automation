/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 2013-2015 ActiveEon
 * 
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * $$ACTIVEEON_INITIAL_DEV$$
 */



package org.ow2.proactive.workflowcatalog.cli;

import org.ow2.proactive.workflowcatalog.cli.cmd.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.cli.Option;

import static org.ow2.proactive.workflowcatalog.cli.CLIException.REASON_INVALID_ARGUMENTS;
import static org.ow2.proactive.workflowcatalog.cli.CLIException.REASON_OTHER;

/**
 * Defines the set of commands and their parameters supported by Scheduler and
 * Resource Manager CLIs.
 */
public class CommandSet {

    public static final CommandSet.Entry URL = CommandSetEntryBuilder
            .newInstance().opt("u").longOpt("url")
            .description("URL of REST server").hasArgs(true).numOfArgs(1)
            .jsExample("")
            .argNames("server-url").jsCommand("url(url)")
            .commandClass(SetUrlCommand.class).entry();

    public static final CommandSet.Entry SESSION = CommandSetEntryBuilder
            .newInstance().opt("si").longOpt("session-id")
            .description("the session id of this session").hasArgs(true)
            .numOfArgs(1).argNames("session-id")
            .jsExample("")
            .jsCommand("setSessionId(sessionId)")
            .commandClass(SetSessionCommand.class).entry();

    public static final CommandSet.Entry LOGIN = CommandSetEntryBuilder
            .newInstance().opt("l").longOpt("login")
            .description("the login name to connect to REST server")
            .hasArgs(true).numOfArgs(1).argNames("login-name")
            .jsCommand("login(login-name)").commandClass(LoginCommand.class)
            .jsExample("login('admin')")
            .entry();

    public static final CommandSet.Entry LOGOUT = CommandSetEntryBuilder
            .newInstance().opt("lo").longOpt("logout")
            .description("logout from the REST server")
            .hasArgs(false)
            .jsCommand("logout()").commandClass(LogoutCommand.class)
            .jsExample("logout()")
            .entry();

    public static final CommandSet.Entry PASSWORD = CommandSetEntryBuilder
            .newInstance().opt("p").longOpt("password")
            .description("the password to connect to REST server")
            .hasArgs(true).numOfArgs(1).argNames("password")
            .jsExample("")
            .commandClass(SetPasswordCommand.class).entry();

    public static final CommandSet.Entry CREDENTIALS = CommandSetEntryBuilder
            .newInstance().opt("c").longOpt("credentials")
            .description("Path to the credential file").hasArgs(true)
            .numOfArgs(1).argNames("cred-path")
            .jsCommand("loginwithcredentials(cred-path)")
            .jsExample("loginwithcredentials('/tmp/rm.cred')")
            .commandClass(LoginWithCredentialsCommand.class).entry();

    public static final CommandSet.Entry CACERTS = CommandSetEntryBuilder
            .newInstance()
            .opt("ca")
            .longOpt("cacerts")
            .description(
                    "CA certificate store (JKS type) to verify peer against (SSL)")
            .hasArgs(true).numOfArgs(1).argNames("store-path")
            .jsCommand("cacerts(store-path)")
            .jsExample("cacerts('/tmp/')")
            .commandClass(SetCaCertsCommand.class).entry();

    public static final CommandSet.Entry CACERTS_PASSWORD = CommandSetEntryBuilder
            .newInstance().opt("cap").longOpt("cacertspass")
            .description("Password for CA certificate store").hasArgs(true)
            .numOfArgs(1).argNames("store-password")
            .jsCommand("cacertspass(cacerts-pass)")
            .jsExample("cacertspass('password')")
            .commandClass(SetCaCertsPassCommand.class).entry();

    public static final CommandSet.Entry INSECURE = CommandSetEntryBuilder
            .newInstance()
            .opt("k")
            .longOpt("insecure")
            .description(
                    "Allow connections to SSL sites without certs verification")
            .jsExample("")
            .commandClass(SetInsecureAccessCommand.class).entry();

    public static final CommandSet.Entry SILENT = CommandSetEntryBuilder
            .newInstance().opt("z").longOpt("silent")
            .description("Runs the command-line client in the silent mode.")
            .jsCommand("silent()")
            .jsExample("silent()")
            .commandClass(SetSilentCommand.class).entry();

    public static final CommandSet.Entry OUTPUT = CommandSetEntryBuilder
            .newInstance()
            .opt("o")
            .longOpt("output-file")
            .description(
                    "Output the result of command execution to specified file")
            .hasArgs(true).numOfArgs(1).commandClass(OutputCommand.class)
            .entry();

    public static final CommandSet.Entry EXIT = CommandSetEntryBuilder
            .newInstance().opt("").longOpt("")
            .description("Exit interactive shell")
            .jsCommand("exit()")
            .jsExample("exit()")
            .commandClass(ExitCommand.class).entry();

    public static final CommandSet.Entry EVAL = CommandSetEntryBuilder
            .newInstance().opt("sf").longOpt("script")
            .description("Evaluate the specified JavaScript file")
            .hasArgs(true)
            .argNames("script-path [param-1=value-1 param-2=value-2 ...]")
            .jsCommand("script(script-pathname,param1=value1,...)")
            .jsExample("script('/tmp/script', [:])")
            .commandClass(EvalScriptCommand.class).entry();

    public static final CommandSet.Entry WORKFLOWS_LIST = CommandSetEntryBuilder
            .newInstance().opt("w").longOpt("listworkflows")
            .description("List the workflows").hasArgs(false)
            .jsCommand("listworkflows()")
            .jsExample("listworkflows()")
            .commandClass(ListWorkflowsCommand.class).entry();

    public static final CommandSet.Entry SUBMIT_WORKFLOW = CommandSetEntryBuilder
            .newInstance().opt("ww").longOpt("submitworkflow")
            .description("Submit the workflow")
            .hasArgs(true)
            .numOfArgs(3)
            .argNames("workflowname variables genericInformation")
            .jsCommand("submitworkflow(workflowname, variables, genericInformation)")
            .jsExample("submitworkflow('workflowname.xml', [var1:'val1', var2:'val2'], " +
                               "[gInfo1:'gInfo1'])")
            .commandClass(SubmitWorkflowCommand.class).entry();

    public static final CommandSet.Entry GET_JOB_RESULT = CommandSetEntryBuilder
            .newInstance().opt("rr").longOpt("getjobresult")
            .description("Get the result of the job")
            .hasArgs(true)
            .numOfArgs(1)
            .argNames("jobid")
            .jsCommand("getjobresult(jobid)")
            .jsExample("getjobresult(33)")
            .commandClass(GetJobResultCommand.class).entry();

    public static final CommandSet.Entry GET_JOB_LOGS = CommandSetEntryBuilder
            .newInstance().opt("rr").longOpt("getjoblogs")
            .description("Get the logs of the job")
            .hasArgs(true)
            .numOfArgs(1)
            .argNames("jobid")
            .jsCommand("getjoblogs(jobid)")
            .jsExample("getjoblogs(33)")
            .commandClass(GetJobLogsCommand.class).entry();

    public static final CommandSet.Entry WC_IMODE = CommandSetEntryBuilder
            .newInstance().opt("i").longOpt("interactive")
            .description("Interactive mode of REST CLI")
            .jsExample("")
            .commandClass(WcImodeCommand.class).entry();

    public static final CommandSet.Entry WC_HELP = CommandSetEntryBuilder
            .newInstance()
            .opt("h")
            .longOpt("help")
            .description(
                    "Prints the usage of REST command-line client for Resource Manager")
            .jsExample("")
            .commandClass(WcHelpCommand.class).entry();

    public static final CommandSet.Entry WC_JS_HELP = CommandSetEntryBuilder
            .newInstance().opt("").longOpt("").description("Interactive help")
            .jsCommand("help()").commandClass(WcJsHelpCommand.class)
            .jsExample("help()")
            .entry();

    public static final CommandSet.Entry UPLOAD_FILE = CommandSetEntryBuilder
            .newInstance().opt("pf")
            .longOpt("uploadfile")
            .description("Push the specified file to the specified location of the server").hasArgs(
                    true)
            .numOfArgs(4)
            .argNames("local-file space-name file-path file-name")
            .jsCommand("uploadfile(local-file,GLOBALSPACE/USERSPACE,file-path,file-name)")
            .jsExample("uploadfile('/tmp/a.txt','USERSPACE','.','a.txt')")
            .commandClass(UploadFileCommand.class).entry();

    public static final CommandSet.Entry DOWNLOAD_FILE = CommandSetEntryBuilder.newInstance().opt("gf")
            .longOpt("downloadfile")
            .description("Retrieves the specified file from the server and stores it locally.").hasArgs(true)
            .numOfArgs(3).argNames("space-name path-name local-file")
            .jsCommand("downloadfile(GLOBALSPACE/USERSPACE,path-name,local-file)")
            .jsExample("downloadfile('GLOBALSPACE', '/a.txt', '/tmp/a.txt')")
            .commandClass(DownloadFileCommand.class).entry();

    /**
     * CommandSet.Entry objects for this CLI.
     */
    public static final CommandSet.Entry[] COMMON_COMMANDS = new CommandSet.Entry[] {
            URL, SESSION, PASSWORD, CREDENTIALS, INSECURE,
            CACERTS, CACERTS_PASSWORD, EVAL, SILENT, OUTPUT,
            LOGIN, LOGOUT, WORKFLOWS_LIST, SUBMIT_WORKFLOW, GET_JOB_RESULT, GET_JOB_LOGS,
            WC_IMODE, WC_HELP, WC_JS_HELP, DOWNLOAD_FILE, UPLOAD_FILE};

    private CommandSet() {
    }

    /**
     * Description of a specific command.
     */
    public static class Entry implements Comparable<Entry> {

        /**
         * Name of this option.
         */
        private String opt;
        /**
         * Long name of this option.
         */
        private String longOpt;
        /**
         * Description of this option.
         */
        private String description;
        /**
         * Interactive shell command description of this option.
         */
        private String jsCommand = null;
        /**
         * Interactive shell command example for this option.
         */
        private String jsExample = null;
        /**
         * Indicates whether this option has one or more arguments.
         */
        private boolean hasArgs = false;
        /**
         * Argument names of this option.
         */
        private String argNames;
        /**
         * Number of arguments of this option.
         */
        private int numOfArgs = -1;
        /**
         * Name of the Command class responsible for processing this option.
         */
        private Class<?> commandClass;

        private Entry() {
        }

        /**
         * Returns the name of this option.
         * 
         * @return the name of this option.
         */
        public String opt() {
            return opt;
        }

        /**
         * Sets the name of this option.
         * 
         */
        private void setOpt(String opt) {
            this.opt = opt;
        }

        /**
         * Returns the long name of this option.
         * 
         * @return the long name of this option.
         */
        public String longOpt() {
            return longOpt;
        }

        /**
         * Sets the long name of this option.
         * 
         * @param longOpt
         *            the long name of this option.
         */
        private void setLongOpt(String longOpt) {
            this.longOpt = longOpt;
        }

        /**
         * Returns the description of this option.
         * 
         * @return the description of this option.
         */
        public String description() {
            return description;
        }

        /**
         * Sets the description of this option.
         * 
         * @param description
         *            the description of this option.
         */
        private void setDescription(String description) {
            this.description = description;
        }

        /**
         * Returns the description of interactive shell command of this option.
         * 
         * @return the interactive shell command description.
         */
        public String jsCommand() {
            return jsCommand;
        }

        /**
         * Sets the description of interactive shell command of this option.
         * 
         * @param jsCommand
         *            the interactive shell command description.
         */
        private void setJsCommand(String jsCommand) {
            this.jsCommand = jsCommand;
        }

        /**
         * Returns an example of interactive shell command of this option.
         *
         * @return the interactive shell command example.
         */
        public String jsExample() {
            return jsExample;
        }

        /**
         * Sets the example of interactive shell command of this option.
         *
         * @param jsExample
         *            the interactive shell command example.
         */
        private void setJsExample(String jsExample) {
            this.jsExample = jsExample;
        }

        /**
         * Returns <tt>ture</tt> if this option has one or more arguments.
         * 
         * @return <tt>true</tt> if this option has one or more arguments.
         */
        public boolean hasArgs() {
            return hasArgs;
        }

        /**
         * Sets whether this option has one or more arguments.
         * 
         * @param hasArgs
         *            whether this option has one or more arguments.
         */
        private void setHasArgs(boolean hasArgs) {
            this.hasArgs = hasArgs;
        }

        /**
         * Returns the argument names of this option.
         * 
         * @return the argument names.
         */
        public String argNames() {
            return argNames;
        }

        /**
         * Sets the argument names of this option.
         * 
         * @param argNames
         *            the argument names.
         */
        public void setArgNames(String argNames) {
            this.argNames = argNames;
        }

        /**
         * Returns the number of arguments of this option. If not specified,
         * returns <tt>-1</tt>.
         * 
         * @return the number of arguments of this option.
         */
        public int numOfArgs() {
            return numOfArgs;
        }

        /**
         * Sets the number of arguments of this option.
         * 
         * @param numOfArgs
         *            the number of arguments of this option.
         */
        private void setNumOfArgs(int numOfArgs) {
            this.numOfArgs = numOfArgs;
        }

        /**
         * Returns an instance of Command class which is responsible for
         * processing this option along with any arguments specified.
         * 
         * @param option
         *            a wrapper instance which contains arguments of this
         *            option.
         */
        public Command commandObject(Option option) {
            return CommandSetEntryHelper.newCommandObject(option, commandClass);
        }

        /**
         * Sets the name of the Command type responsible for processing this
         * option.
         * 
         * @param commandClass
         *            the class name of the Command type.
         */
        public void setCommandClass(Class<?> commandClass) {
            this.commandClass = commandClass;
        }

        @Override
        public int compareTo(Entry o) {
            return opt.compareTo(o.opt);
        }
    }

    private static class CommandSetEntryHelper {
        private CommandSetEntryHelper() {
        }

        public static Command newCommandObject(Option opt, Class<?> commandClass) {
            try {
                Constructor<?>[] ctors = commandClass.getConstructors();
                if (ctors.length > 0) {
                    String[] values = opt.getValues();
                    int numOfCtorArgs = (values == null) ? 0 : values.length;

                    for (Constructor<?> ctor : ctors) {
                        // naive way of selecting the most suitable ctor
                        Class<?>[] paramTypes = ctor.getParameterTypes();
                        if (paramTypes.length == 1
                                && (String[].class.equals(paramTypes[0]))) {
                            return (Command) ctor.newInstance((Object) values);

                        } else if (paramTypes.length == numOfCtorArgs) {
                            return newInstance(ctor, values);
                        }
                    }
                }

                throw new CLIException(
                        REASON_INVALID_ARGUMENTS,
                        String.format(
                                "%s %s %s:%n%s %s.",
                                "No suitable command found for",
                                opt.getOpt(),
                                opt.getValuesList(),
                                "Check whether you have specified all required arguments for",
                                opt.getOpt()));

            } catch (CLIException error) {
                throw error;
            } catch (IllegalArgumentException error) {
                throw new CLIException(REASON_INVALID_ARGUMENTS, error);
            } catch (Exception error) {
                throw new CLIException(REASON_OTHER, error);
            }
        }

        private static Command newInstance(Constructor<?> ctor, String[] args)
                throws IllegalArgumentException, InstantiationException,
                IllegalAccessException, InvocationTargetException {
            Object[] ctorArgs = new Object[ctor.getParameterTypes().length];
            if (args != null) {
                System.arraycopy(args, 0, ctorArgs, 0, args.length);
            }
            return (Command) ctor.newInstance(ctorArgs);
        }
    }

    /**
     * Utility class to build CommandSet.Entry objects.
     */
    private static class CommandSetEntryBuilder {

        private CommandSet.Entry entry = null;

        private CommandSetEntryBuilder() {
            entry = new CommandSet.Entry();
        }

        public static CommandSetEntryBuilder newInstance() {
            return new CommandSetEntryBuilder();
        }

        public CommandSetEntryBuilder opt(String opt) {
            entry.setOpt(opt);
            return this;
        }

        public CommandSetEntryBuilder longOpt(String longOpt) {
            entry.setLongOpt(longOpt);
            return this;
        }

        public CommandSetEntryBuilder description(String description) {
            entry.setDescription(description);
            return this;
        }

        public CommandSetEntryBuilder hasArgs(boolean hasArgs) {
            entry.setHasArgs(hasArgs);
            return this;
        }

        public CommandSetEntryBuilder numOfArgs(int numOfArgs) {
            entry.setNumOfArgs(numOfArgs);
            return this;
        }

        public CommandSetEntryBuilder argNames(String argNames) {
            entry.setArgNames(argNames);
            return this;
        }

        public CommandSetEntryBuilder jsCommand(String jsCommand) {
            entry.setJsCommand(jsCommand);
            return this;
        }

        public CommandSetEntryBuilder jsExample(String jsExample) {
            entry.setJsExample(jsExample);
            return this;
        }

        public CommandSet.Entry entry() {
            return entry;
        }

        public CommandSetEntryBuilder commandClass(Class<?> commandClass) {
            entry.setCommandClass(commandClass);
            return this;
        }
    }

}
