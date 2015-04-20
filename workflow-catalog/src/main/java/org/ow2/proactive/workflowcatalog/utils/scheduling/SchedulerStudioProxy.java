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
