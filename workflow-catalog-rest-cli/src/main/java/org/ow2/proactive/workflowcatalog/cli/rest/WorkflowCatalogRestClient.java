package org.ow2.proactive.workflowcatalog.cli.rest;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.ow2.proactive.workflowcatalog.api.RestApi;
import org.ow2.proactive.workflowcatalog.api.exceptions.ExceptionFormatterUtils;

import javax.ws.rs.ServerErrorException;
import java.lang.reflect.*;

public class WorkflowCatalogRestClient extends ClientBase implements WorkflowCatalogClient {

    private RestApi proxy;

    private WorkflowCatalogRestClient() {}

    public static WorkflowCatalogClient createInstance() {
        return new WorkflowCatalogRestClient();
    }

    public void init(String url, String login, String password) throws Exception {
        proxy = createRestApi(url, null);
    }

    @Override
    public void init(String url, String sessionId) throws Exception {
        init(url, null, null);
    }

    public RestApi getProxy() {
        return proxy;
    }

    static class Handler implements InvocationHandler {

        public RestApi api;

        public Handler(RestApi api) {
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
                } catch (Exception e) {
                    throw e;
                }
            }
        }

        private Throwable reconstructExceptionFromEmbeddedException(ServerErrorException e) {
            return ExceptionFormatterUtils.createException(e.getResponse());
        }
    }

    private RestApi createRestApi(String url, String sessionId) {

        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(url);
        RestApi proxy = target.proxy(RestApi.class);

        Handler handler = new Handler(proxy);
        Class[] interfacesArray = new Class[] {RestApi.class};

        return (RestApi) Proxy.newProxyInstance(RestApi.class.getClassLoader(), interfacesArray, handler);
    }



}
