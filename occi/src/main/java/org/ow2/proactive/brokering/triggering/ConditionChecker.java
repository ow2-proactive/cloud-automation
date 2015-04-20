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

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import static org.ow2.proactive.brokering.occi.categories.trigger.ActionTrigger.*;

public class ConditionChecker extends TimerTask {

    private static final Logger logger = Logger.getLogger(ConditionChecker.class.getName());

    private Map<String, String> args;
    private Class conditionScript;
    private Class actionCaseTrue;
    private Class actionCaseFalse;

    public ConditionChecker(Map<String, String> args, Actions actions, Conditions conditions) throws ScriptException {
        try {
            conditionScript = ScriptUtils.getEncodedScriptAsClass(args, conditions, OCCI_CONDITION_SCRIPT);
            actionCaseTrue = ScriptUtils.getEncodedScriptAsClass(args, actions, OCCI_MONITORING_TRUEACTION);
            if (isScriptProvided(args, OCCI_MONITORING_FALSEACTION))
                actionCaseFalse = ScriptUtils.getEncodedScriptAsClass(args, actions, OCCI_MONITORING_FALSEACTION);
        } catch (ScriptException e) {
            logger.info("Bad script", e);
            throw e;
        }

        this.args = args;
    }

    @Override
    public void run() {
        try {
            Boolean conditionResult = checkCondition(conditionScript);

            if (conditionResult == null)
                return;

            if (conditionResult)
                executeAction(actionCaseTrue);
            else
                executeAction(actionCaseFalse);

        } catch (ScriptException e) {
            logger.debug("Failure in execution of ConditionChecker", e);
        }
    }

    private Boolean checkCondition(Class script) throws ScriptException {
        try {
            Condition cond = (Condition) script.newInstance();
            return cond.evaluate(args);
        } catch (Throwable e) {
            logger.debug("Error when checking condition: " + script, e);
            throw new ScriptException(e);
        }
    }

    private void executeAction(Class script) throws ScriptException {
        if (script == null)
            return;

        try {
            Action cond = (Action) script.newInstance();
            cond.execute(args);
        } catch (Throwable e) {
            logger.warn("Error when executing action: " + script, e);
            throw new ScriptException(e);
        }
    }

    private boolean isScriptProvided(Map<String, String> args, String key) {
        return args.containsKey(key) && !args.get(key).isEmpty();
    }

}

