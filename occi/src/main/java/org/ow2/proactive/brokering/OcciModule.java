/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.brokering;


import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import org.ow2.proactive.brokering.occi.OcciServer;
import org.ow2.proactive.brokering.occi.api.Occi;
import org.ow2.proactive.brokering.occi.categories.Utils;
import org.ow2.proactive.brokering.occi.client.ActionTriggerHandler;
import org.ow2.proactive.brokering.occi.database.DatabaseFactory;
import org.ow2.proactive.brokering.updater.Updater;
import org.ow2.proactive.workflowcatalog.Catalog;
import org.ow2.proactive.workflowcatalog.FileCatalog;
import org.ow2.proactive.workflowcatalog.RestAuthentication;
import org.ow2.proactive.workflowcatalog.security.SchedulerRestSession;
import org.ow2.proactive.workflowcatalog.utils.scheduling.ISchedulerProxy;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerLoginData;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerProxy;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;


public class OcciModule implements Module {
    public void configure(final Binder binder) {
        Names.bindProperties(binder, readConfigurationProperties());

//        binder.bind(ISchedulerProxy.class).to(MiniScheduler.class).in(Singleton.class);
        binder.bind(DatabaseFactory.class).in(Singleton.class);

        binder.bind(RestAuthentication.class).to(OcciSchedulerAuthentication.class);
//        binder.bind(RestAuthentication.class).to(MiniScheduler.Authentication.class);
        binder.bind(Occi.class).to(OcciServer.class);
        binder.bind(Broker.class).in(Singleton.class);
        binder.bind(Updater.class).in(Singleton.class);
    }

    @Provides
    ISchedulerProxy createScheduler(
      @Named("scheduler.url") String schedulerUrl,
      @Named("scheduler.username") String schedulerUsername,
      @Named("scheduler.password") String schedulerPassword,
      @Named("scheduler.insecuremode") boolean insecureMode) throws LoginException, SchedulerRestException {
        return new SchedulerProxy(
          new SchedulerLoginData(schedulerUrl, schedulerUsername, schedulerPassword, insecureMode));
    }

    @Provides
    SchedulerFactory schedulerClientOfConnectedUser() {
        return new SchedulerFactory() {
            @Override
            public ISchedulerProxy getScheduler() {
                return SchedulerRestSession.getScheduler();
            }
        };
    }

    @Provides
    Catalog createCatalog(DatabaseFactory databaseFactory,
      @Named("catalog.path") String configCatalogPath,
      @Named("catalog.refresh.ms") long configCatalogRefresh) {
        File catalogPath = Utils.getScriptsPath(configCatalogPath, "/config/catalog-test");
        return new FileCatalog(catalogPath, configCatalogRefresh, new CatalogToResource(databaseFactory));
    }

    @Provides
    ActionTriggerHandler createActionTriggerHandler(
      @Named("actions.path") String actionsPath,
      @Named("actions.refresh") long actionsRefresh,
      @Named("conditions.path") String conditionsPath,
      @Named("conditions.refresh") long conditionsRefresh) {
        return new ActionTriggerHandler(actionsPath, actionsRefresh, conditionsPath, conditionsRefresh);
    }

    @Provides
    Rules createRules(@Named("rules.path") String configRulesPath,
      @Named("rules.refresh.ms") long configRulesRefresh) {
        File rulesPath = Utils.getScriptsPath(configRulesPath, "/config/rules");
        return new Rules(rulesPath, configRulesRefresh);
    }

    private Properties readConfigurationProperties() {
        Properties props = new Properties();
        try {
            props.load(getClass().getResourceAsStream("/config/configuration.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read configuration", e);
        }
        return props;
    }
}
