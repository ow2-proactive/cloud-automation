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


package org.ow2.proactive.brokering;

import org.ow2.proactive.brokering.occi.OcciServer;
import org.ow2.proactive.brokering.occi.database.Database;
import org.ow2.proactive.brokering.occi.database.DatabaseFactory;
import org.ow2.proactive.brokering.occi.database.InMemoryDB;
import org.ow2.proactive.workflowcatalog.utils.scheduling.ISchedulerProxy;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;
import org.junit.Test;

import static org.junit.Assert.*;


public class OcciModuleTest {

    @Test
    public void OcciServer_can_be_started_with_Guice() throws Exception {
        Injector injector = Guice.createInjector(Modules.override(new OcciModule()).with(new InMemoryDBModule()));
        assertNotNull(injector.getInstance(OcciServer.class));
    }

    private class InMemoryDBModule implements Module {
        @Override
        public void configure(Binder binder) {
            binder.bind(DatabaseFactory.class).toInstance(new DatabaseFactory(){
                @Override
                public Database build() {
                    return new InMemoryDB();
                }
            });
            binder.bind(ISchedulerProxy.class).to(MiniScheduler.class).in(Singleton.class);
        }
    }
}
