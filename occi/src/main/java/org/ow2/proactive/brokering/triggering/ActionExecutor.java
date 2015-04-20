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


package org.ow2.proactive.brokering.triggering;

import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.occi.categories.trigger.ActionTrigger;
import java.util.Map;

public class ActionExecutor extends Thread {

    private static final Logger logger = Logger.getLogger(ActionExecutor.class.getName());

    private Class action;
    private Map<String, String> args;

    public ActionExecutor(Map<String, String> args, Actions actions, String key) throws ScriptException {
        this.args = args;
        this.action = ScriptUtils.getEncodedScriptAsClass(args, actions, key);
    }

    private void executeAction(Class script) {
        if (script == null)
            return;

        try {
            Action action = (Action) script.newInstance();
            action.execute(args);
        } catch (Throwable e) {
            logger.debug("Error executing action: " + script, e);
        }
    }

    @Override
    public void run() {
        executeAction(action);
    }

}
