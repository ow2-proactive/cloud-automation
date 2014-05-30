package org.ow2.proactive.brokering;

import org.ow2.proactive.brokering.occi.OcciServer;
import org.ow2.proactive.brokering.occi.database.Database;
import org.ow2.proactive.brokering.occi.database.DatabaseFactory;
import org.ow2.proactive.brokering.occi.database.InMemoryDB;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
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
        }
    }
}