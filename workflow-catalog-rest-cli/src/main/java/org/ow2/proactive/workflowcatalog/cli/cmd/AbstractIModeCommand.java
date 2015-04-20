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



package org.ow2.proactive.workflowcatalog.cli.cmd;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.ow2.proactive.workflowcatalog.cli.ApplicationContext;
import org.ow2.proactive.workflowcatalog.cli.CLIException;
import org.ow2.proactive.workflowcatalog.cli.utils.StringUtility;

public abstract class AbstractIModeCommand extends AbstractCommand implements
        Command {

    public static final String TERMINATE = "org.ow2.proactive.workflowcatalog.cli.cmd.AbstractIModeCommand.terminate";

    public AbstractIModeCommand() {
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        ScriptEngine engine = currentContext.getEngine();
        try {
            // load supported functions
            engine.eval(new InputStreamReader(script()));

        } catch (ScriptException error) {
            throw new CLIException(CLIException.REASON_OTHER, error);
        }

        while (!currentContext.getProperty(TERMINATE, Boolean.TYPE, false)) {
            try {
                String command = readLine(currentContext, "> ");
                if (command == null)
                    break; // EOF, exit interactive shell
                CommandHelper helper = new CommandHelper();
                String commandTuned = helper.tune(currentContext, command);
                engine.eval(commandTuned);
            } catch (ScriptException se) {
                writeLine(currentContext, "%s\n%s",
                        "An error occurred while executing the script:",
                        StringUtility.stackTraceAsString(se));
            }
        }
    }

    protected abstract InputStream script();


    private class CommandHelper {

        public String tune(ApplicationContext context, String command) {
            command = fixSingleCommandWOParenthesis(context, command);
            command = fixParamsCommandWithoutQuotations(context, command);
            return command;
        }

        private String fixSingleCommandWOParenthesis(ApplicationContext context,
                                                           String command) {
            String tuned;
            command = command.trim();
            if (!command.isEmpty() &&
                    !command.contains(" ") &&
                    !command.contains("(")) {
                tuned = command + "()";
                writeLine(context, "Warning: replacing command '%s' by '%s'", command, tuned);
            } else {
                tuned = command;
            }
            return tuned;
        }

        private String fixParamsCommandWithoutQuotations(ApplicationContext context,
                                                           String command) {
            String tuned;
            command = command.trim();
            if (!command.isEmpty() && command.contains(" ")
                    && !command.contains("'") && !command.contains("\"")) {
                String[] parts = command.split(" ");
                StringBuilder builder = new StringBuilder();
                for (String part: parts) {
                    if (builder.length() == 0 ) {
                        builder.append(part);
                        builder.append("(");
                    } else {
                        builder.append("'");
                        builder.append(part);
                        builder.append("'");
                        builder.append(",");
                    }
                }
                builder.append(")");
                tuned = builder.toString();
                writeLine(context, "Warning: replacing command \"%s\" by command \"%s\"", command, tuned);
            } else {
                tuned = command;
            }
            return tuned;
        }
    }
}

