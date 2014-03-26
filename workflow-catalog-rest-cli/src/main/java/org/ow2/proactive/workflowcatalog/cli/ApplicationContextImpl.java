package org.ow2.proactive.workflowcatalog.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.ow2.proactive.workflowcatalog.cli.console.AbstractDevice;
import org.codehaus.jackson.map.ObjectMapper;

import static org.ow2.proactive.workflowcatalog.cli.CLIException.REASON_OTHER;

public class ApplicationContextImpl implements ApplicationContext {

    private static final String DEVICE = "org.ow2.proactive.workflowcatalog.cli.ApplicationContextImpl.deviceImpl";
    private static final String SCRIPT_ENGINE = "org.ow2.proactive.workflowcatalog.cli.ApplicationContextImpl.scriptEngine";
    private static final String OBJECT_MAPPER = "org.ow2.proactive.workflowcatalog.cli.ApplicationContextImpl.objectMapper";
    private static final String INFRASTRUCTURES = "org.ow2.proactive.workflowcatalog.cli.ApplicationContextImpl.infrastructures";
    private static final String POLICIES = "org.ow2.proactive.workflowcatalog.cli.ApplicationContextImpl.policies";
    private static final String RESULT_STACK = "org.ow2.proactive.workflowcatalog.cli.ApplicationContextImpl.resultStack";

    private static final ApplicationContextHolder threadLocalContext = new ApplicationContextHolder();

    private String sessionId = "";
    private String restServerUrl;
    //private SchedulerRestClient restClient;
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

    /*
	@Override
	public SchedulerRestClient getRestClient() {
		HttpClient client = HttpUtility.threadSafeClient();
		if (canInsecureAccess()) {
			try {
				HttpUtility.setInsecureAccess(client);
			} catch (Exception e) {
				throw new CLIException(REASON_OTHER,
						"Cannot disable SSL verification.", e);
			}
		}
		restClient = new SchedulerRestClient(restServerUrl,
				new ApacheHttpClient4Executor(client));
		return restClient;
	}
    */

    @Override
    public String getResourceUrl(String resource) {
        return (new StringBuilder()).append(getRestServerUrl()).append('/').append(resource).toString();
    }

    @Override
    public String getRestServerUrl() {
        return restServerUrl;
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
