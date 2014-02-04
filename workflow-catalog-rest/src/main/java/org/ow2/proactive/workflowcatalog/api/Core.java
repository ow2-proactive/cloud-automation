package org.ow2.proactive.workflowcatalog.api;

import java.util.*;
import org.ow2.proactive.workflowcatalog.Catalog;
import org.ow2.proactive.workflowcatalog.Workflow;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerProxy;
import org.ow2.proactive.workflowcatalog.utils.scheduling.SchedulerLoginData;
import static org.ow2.proactive.workflowcatalog.api.utils.ConfigurationHelper.*;

public class Core {

    private static Core instance;
    public static Core getInstance() {
        if (instance == null)
            instance = new Core();
        return instance;
    }

    private SchedulerLoginData loginData;
    private SchedulerProxy scheduler;
    private Catalog catalog;

    private Core() {
        Configuration config = getConfiguration();
        loginData = getSchedulerLoginData(config);
        scheduler = new SchedulerProxy(loginData);
        catalog = new Catalog(getCatalogPath(config), config.catalog.refresh * 1000);
    }

    public Collection<Workflow> getWorkflows() {
        return catalog.getWorkflows();
    }

}

