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


package org.ow2.proactive.workflowcatalog.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.ow2.proactive.workflowcatalog.cli.console.AbstractDevice;
import org.ow2.proactive.workflowcatalog.cli.rest.WorkflowCatalogClient;
import org.ow2.proactive.workflowcatalog.cli.rest.WorkflowCatalogRestClient;
import org.ow2.proactive.workflowcatalog.utils.HttpUtility;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerRestClient;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;

import static org.ow2.proactive.workflowcatalog.cli.CLIException.REASON_OTHER;

public class ApplicationContextImpl implements ApplicationContext {

    private static final String DEVICE = "org.ow2.proactive.workflowcatalog.cli.ApplicationContextImpl.deviceImpl";
    private static final String SCRIPT_ENGINE = "org.ow2.proactive.workflowcatalog.cli.ApplicationContextImpl.scriptEngine";
    private static final String OBJECT_MAPPER = "org.ow2.proactive.workflowcatalog.cli.ApplicationContextImpl.objectMapper";
    private static final String RESULT_STACK = "org.ow2.proactive.workflowcatalog.cli.ApplicationContextImpl.resultStack";

    private static final ApplicationContextHolder threadLocalContext = new ApplicationContextHolder();

    private String sessionId = "";
    private String restServerUrl;
    private boolean insecureAccess;
    private boolean forced;
    private boolean silent = false;
    private Map<String, Object> properties = new HashMap<String, Object>();

    public static ApplicationContext currentContext() {
        return threadLocalContext.get();
    }

    private ApplicationContextImpl() {
    }

    @Override
    public void setDevice(AbstractDevice deviceImpl) {
        setProperty(DEVICE, deviceImpl);
    }

    @Override
    public AbstractDevice getDevice() {
        return getProperty(DEVICE, AbstractDevice.class);
    }

    @Override
    public void setRestServerUrl(String restServerUrl) {
        if (restServerUrl.endsWith("/")) {
            this.restServerUrl = restServerUrl.substring(0, restServerUrl.length() - 1);
        } else {
            this.restServerUrl = restServerUrl;
        }
    }

	@Override
    public WorkflowCatalogClient getWorkflowCatalogClient() {
        WorkflowCatalogClient client = WorkflowCatalogRestClient.createInstance();
        try {
            client.init(getWcUrl(), sessionId, createHttpClient());
        } catch (Exception e) {
            throw new CLIException(CLIException.REASON_OTHER, "Initialization error", e);
        }
        return client;
    }

    @Override
    public SchedulerRestClient getSchedulerClient() {
        ApacheHttpClient4Engine httpEngine = createHttpClient();
        return new SchedulerRestClient(getSchedulingUrl(), httpEngine);
    }

    private ApacheHttpClient4Engine createHttpClient() {
        HttpClient httpClient = new DefaultHttpClient();
        ClientConnectionManager mgr = httpClient.getConnectionManager();
        HttpParams params = httpClient.getParams();
        httpClient = new DefaultHttpClient(new PoolingClientConnectionManager(
                mgr.getSchemeRegistry()), params);

        if (canInsecureAccess())
            httpClient = HttpUtility.turnClientIntoInsecure(httpClient);

        return new ApacheHttpClient4Engine(httpClient);
    }

    @Override
    public String getWcResourceUrl(String resource) {
        return (new StringBuilder()).append(getWcUrl()).append('/').append(resource).toString();
    }

    @Override
    public String getSchedulingResourceUrl(String resource) {
        return (new StringBuilder()).append(getSchedulingUrl()).append('/').append(resource).toString();
    }

    private String getSchedulingUrl() {
        return restServerUrl + "/scheduling";
    }

    private String getWcUrl() {
        return restServerUrl + "/wc";
    }

    @Override
    public void setObjectMapper(ObjectMapper objectMapper) {
        setProperty(OBJECT_MAPPER, objectMapper);
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return getProperty(OBJECT_MAPPER, ObjectMapper.class);
    }

    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public boolean canInsecureAccess() {
        return insecureAccess;
    }

    @Override
    public void setInsecureAccess(boolean insecureAccess) {
        this.insecureAccess = insecureAccess;
    }

    @Override
    public ScriptEngine getEngine() {
        ScriptEngine engine = getProperty(SCRIPT_ENGINE, ScriptEngine.class);
        if (engine == null) {
            ScriptEngineManager mgr = new ScriptEngineManager();

            engine = mgr.getEngineByExtension("groovy");

            if (engine == null) {
                throw new CLIException(REASON_OTHER,
                        "Cannot obtain JavaScript engine instance.");
            }
            engine.getContext().setWriter(getDevice().getWriter());
            setProperty(SCRIPT_ENGINE, engine);
        }
        return engine;
    }

    @Override
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(String key, Class<T> type) {
        Object object = properties.get(key);
        return (T) object;
    }

    @Override
    public <T> T getProperty(String key, Class<T> type, T dflt) {
        T property = getProperty(key, type);
        return (property == null) ? dflt : property;
    }

    @Override
    public boolean isForced() {
        return forced;
    }

    @Override
    public void setForced(boolean forced) {
        this.forced = forced;
    }

    @Override
    public boolean isSilent() {
        return silent;
    }

    @Override
    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Stack resultStack() {
        Stack resultStack = getProperty(RESULT_STACK, Stack.class);
        if (resultStack == null) {
            resultStack = new Stack();
            setProperty(RESULT_STACK, resultStack);
        }
        return resultStack;
    }

    private static class ApplicationContextHolder extends
            ThreadLocal<ApplicationContextImpl> {
        @Override
        protected ApplicationContextImpl initialValue() {
            return new ApplicationContextImpl();
        }
    }
}
