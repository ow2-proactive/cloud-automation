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

import static org.ow2.proactive.workflowcatalog.cli.CommandSet.INSECURE;
import static org.ow2.proactive.workflowcatalog.cli.CommandSet.LOGIN;
import static org.ow2.proactive.workflowcatalog.cli.CommandSet.PASSWORD;
import static org.ow2.proactive.workflowcatalog.cli.CommandSet.SESSION;
import static org.ow2.proactive.workflowcatalog.cli.CommandSet.SILENT;
import static org.ow2.proactive.workflowcatalog.cli.CommandSet.URL;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.ow2.proactive.workflowcatalog.cli.cmd.Command;

/**
 * {@link CommandFactory} builds an ordered {@link Command} list for a given
 * user argument set.
 */
public abstract class CommandFactory {

    private static final Map<String, CommandSet.Entry> commonCmdMap;

    static {
        commonCmdMap = new HashMap<String, CommandSet.Entry>();
        for (CommandSet.Entry entry : CommandSet.COMMON_COMMANDS) {
            commonCmdMap.put(opt(entry), entry);
        }
    }

    public static CommandFactory getCommandFactory() {
        return new WcCommandFactory();
    }

    static void put(CommandSet.Entry entry, Map<String, CommandSet.Entry> map) {
        map.put(opt(entry), entry);
    }

    static Map<String, CommandSet.Entry> supportedCommandMap() {
        return commonCmdMap;
    }

    static String opt(CommandSet.Entry entry) {
        return entry.opt();
    }

    public abstract Options supportedOptions();

    public abstract List<Command> getCommandList(CommandLine cli, ApplicationContext currentContext);

    public abstract void addCommandEntry(CommandSet.Entry entry);

    public abstract Command commandForOption(Option option);

    public abstract CommandSet.Entry[] supportedCommandEntries();

    /**
     * Returns an ordered {@link Command} list for specified user arguments.
     * 
     * @param cli
     *            the command-line arguments
     * @return an ordered {@link Command} list.
     */
    protected List<Command> getCommandList(CommandLine cli,
            Map<String, Command> map, ApplicationContext currentContext) {
        LinkedList<Command> list = new LinkedList<Command>();

        if (map.containsKey(opt(SILENT))) {
            list.add(map.remove(opt(SILENT)));
        }

        if (map.containsKey(opt(URL))) {
            list.addFirst(map.remove(opt(URL)));
        }

        if (map.containsKey(opt(INSECURE))) {
            list.add(map.remove(opt(INSECURE)));
        }

        if (map.containsKey(opt(SESSION))) {
            list.add(map.remove(opt(SESSION)));
        }

        if (map.containsKey(opt(PASSWORD))) {
            list.add(map.remove(opt(PASSWORD)));
        }

        if (map.containsKey(opt(LOGIN))) {
            list.add(map.remove(opt(LOGIN)));
        }

        return list;
    }

    protected Map<String, Command> commandMapInstance(CommandLine cli,
            final Map<String, CommandSet.Entry> supportedCommandEntryMap) {
        Map<String, Command> cliCommands = new HashMap<String, Command>();
        for (Option o : cli.getOptions()) {
            cliCommands.put(o.getOpt(),
                    getCommandForOption(o, supportedCommandEntryMap));
        }
        return cliCommands;
    }

    protected Command getCommandForOption(Option opt,
            final Map<String, CommandSet.Entry> commandMap) {
        return commandMap.get(opt.getOpt()).commandObject(opt);
    }

    protected Options createOptions(Collection<CommandSet.Entry> entries) {
        Options options = new Options();
        for (CommandSet.Entry entry : entries) {
            Option option = new Option(entry.opt(), entry.longOpt(),
                    entry.hasArgs(), entry.description());
            if (entry.numOfArgs() > 0) {
                option.setArgs(entry.numOfArgs());
            } else if (entry.hasArgs() && entry.numOfArgs() == -1) {
                option.setArgs(Integer.MAX_VALUE);
            }
            if (entry.argNames() != null) {
                option.setArgName(entry.argNames());
            }
            options.addOption(option);
        }
        return options;
    }

}
