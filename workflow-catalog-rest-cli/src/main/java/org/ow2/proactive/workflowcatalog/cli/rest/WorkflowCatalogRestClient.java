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


package org.ow2.proactive.workflowcatalog.cli.rest;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import org.ow2.proactive.workflowcatalog.api.Workflows;
import org.ow2.proactive.workflowcatalog.api.exceptions.ExceptionFormatterUtils;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class WorkflowCatalogRestClient extends ClientBase implements WorkflowCatalogClient {

    private Workflows workflows;

    private WorkflowCatalogRestClient() {}

    public static WorkflowCatalogClient createInstance() {
        return new WorkflowCatalogRestClient();
    }

    @Override
    public void init(String url, String sessionId, ClientHttpEngine httpClient) throws Exception {
        workflows = createWorkflowsApi(url, sessionId, httpClient);
    }

    public Workflows getWorkflowsProxy() {
        return workflows;
    }

    static class Handler implements InvocationHandler {

        public Object api;

        public Handler(Object api) {
            this.api = api;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(api, args);
            } catch (InvocationTargetException ex) {
                try {
                    throw ex.getTargetException();
                } catch (ServerErrorException e) {
                    throw reconstructExceptionFromEmbeddedException(e);
                }
            }
        }

        private Throwable reconstructExceptionFromEmbeddedException(ServerErrorException e) {
            return ExceptionFormatterUtils.createException(e.getResponse());
        }
    }

    private Workflows createWorkflowsApi(String url, final String sessionId, ClientHttpEngine httpClient) {

        ResteasyClient client = new ResteasyClientBuilder().register(new ClientRequestFilter() {
            @Override
            public void filter(ClientRequestContext requestContext) throws IOException {
                requestContext.getHeaders().add("sessionid", sessionId);
            }
        }).httpEngine(httpClient).build();
        ResteasyWebTarget target = client.target(url);
        Workflows proxy = target.proxy(Workflows.class);

        Handler handler = new Handler(proxy);
        Class[] interfacesArray = new Class[] { Workflows.class };

        return (Workflows) Proxy.newProxyInstance(Workflows.class.getClassLoader(), interfacesArray, handler);
    }

}
