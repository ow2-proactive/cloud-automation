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
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;


public class WorkflowCatalogRestClient extends ClientBase implements WorkflowCatalogClient {

    private Workflows proxy;

    private WorkflowCatalogRestClient() {}

    public static WorkflowCatalogClient createInstance() {
        return new WorkflowCatalogRestClient();
    }

    public void init(String url, String login, String password) throws Exception {

        String sessionId = "123"; // TODO login
        proxy = createRestApi(url, sessionId);
    }

    @Override
    public void init(String url, String sessionId) throws Exception {
        proxy = createRestApi(url, sessionId);
    }

    public Workflows getProxy() {
        return proxy;
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

    private Workflows createRestApi(String url, final String sessionId) {

        ResteasyClient client = new ResteasyClientBuilder().register(new ClientRequestFilter() {
            @Override
            public void filter(ClientRequestContext requestContext) throws IOException {
                requestContext.getHeaders().add("sessionid", sessionId);
            }
        }).build();
        ResteasyWebTarget target = client.target(url);
        Workflows proxy = target.proxy(Workflows.class);

        Handler handler = new Handler(proxy);
        Class[] interfacesArray = new Class[] { Workflows.class };

        return (Workflows) Proxy.newProxyInstance(Workflows.class.getClassLoader(), interfacesArray, handler);
    }


}
