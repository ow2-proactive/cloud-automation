package org.ow2.proactive.workflowcatalog.utils.scheduling;

import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerRestClient;
import org.ow2.proactive_grid_cloud_portal.studio.StudioInterface;

public class SchedulerStudioProxy {

    private StudioInterface studio;

    public SchedulerStudioProxy (String restEndpointURL, ClientHttpEngine httpEngine) {
        ResteasyProviderFactory provider = ResteasyProviderFactory.getInstance();
        provider.registerProvider(SchedulerRestClient.JacksonContextResolver.class);
        this.studio = createStudioRestProxy(provider, restEndpointURL, httpEngine);
    }

    public StudioInterface getStudio() {
        return studio;
    }

    private static StudioInterface createStudioRestProxy(
            ResteasyProviderFactory provider, String restEndpointURL, ClientHttpEngine httpEngine) {
        ResteasyClient client = new ResteasyClientBuilder().providerFactory(provider).httpEngine(httpEngine).build();
        ResteasyWebTarget target = client.target(restEndpointURL);
        return target.proxy(StudioInterface.class);
    }

}
