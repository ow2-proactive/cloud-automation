package org.ow2.proactive.brokering;

import groovy.lang.GroovyClassLoader;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class Broker {
    private static final Logger logger = Logger.getLogger(Broker.class.getName());
    private static Broker instance;
    private String schedulerUrl;
    private String schedulerUsername;
    private String schedulerPassword;
    private Catalog catalog;
    private Rules rules;
    private HttpClient httpClient;

    private Broker() {
        try {
            File configFile = new File(Broker.class.getResource("/config/configuration.xml").getFile());
            JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Configuration config = (Configuration) jaxbUnmarshaller.unmarshal(configFile);

            schedulerUrl = config.scheduler.url;
            schedulerUsername = config.scheduler.username;
            schedulerPassword = config.scheduler.password;

            httpClient = new DefaultHttpClient();

            File catalogPath = new File(config.catalog.path);
            if (!catalogPath.isDirectory()) {
                catalogPath = new File(Broker.class.getResource("/config/catalog").getFile());
            }

            File rulesPath = new File(config.rules.path);
            if (!rulesPath.isDirectory()) {
                rulesPath = new File(Broker.class.getResource("/config/rules").getFile());
            }

            catalog = new Catalog(catalogPath, config.catalog.refresh * 1000);
            rules = new Rules(rulesPath, config.rules.refresh * 1000);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public static Broker getInstance() {
        // TODO : Double check locking
        if (instance == null) {
            instance = new Broker();
        }
        return instance;
    }

    public boolean request(String category, String operation, Map<String, String> attributes) throws Exception {
        return request(category, operation, null, attributes);
    }

    public boolean request(String category, String operation, String action, Map<String, String> attributes) throws Exception {
        logger.debug("Request       : category=" + category + ", operation=" + operation + " action=" + action);
        logger.debug("   attributes : " + showSorted(attributes));

        for (Workflow workflow : catalog.getWorkflows()) {
            if (workflow.isCompliant(category, operation, action, attributes)) {
                int appliedRules = this.applyRules(attributes, rules);
//              if (workflow.isDeepCompliant(attributes))
                File jobFile = workflow.configure(attributes);
                logger.debug("Generated job file : " + jobFile.getAbsolutePath());
                logger.info("Workflow '" + workflow + "' configured (" + appliedRules + " rules applied) and submitted (Job ID=" + 1234 + ")");
                submitToScheduler(jobFile);
                return true; // TODO: This can be configured to allow multiple workflows submissions
            }
        }
        logger.info("No matching workflow");
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    //
    //                  PRIVATE METHODS
    //
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Apply proper rules
     *
     * @param attributes
     * @param rules
     * @return the number of rules applied
     */
    private int applyRules(Map<String, String> attributes, Rules rules) {
        int count = 0;

        List<File> ruleFiles = rules.getRules();
        GroovyClassLoader gcl = new GroovyClassLoader();
        for (File file : ruleFiles) {
            try {
                Class clazz = gcl.parseClass(file);
                Rule rule = (Rule) clazz.newInstance();
                if (rule.match(attributes)) {
                    rule.apply(attributes);
                    count++;
                }
            } catch (Throwable e) {
                e.printStackTrace();
                logger.debug("Error when loading a rules file : " + file.getName(), e);
            }
        }
        return count;
    }

    private String submitToScheduler(File jobFile) throws AuthenticationException, IOException {
        // Submit selected workflow to the Scheduler
        String sessid = connectToScheduler(schedulerUrl, schedulerUsername, schedulerPassword);
        String endpoint = schedulerUrl + "/scheduler/submit";
        HttpPost post = new HttpPost(endpoint);
        post.addHeader("sessionid", sessid);
        MultipartEntity me = new MultipartEntity();
        me.addPart("descriptor", new FileBody(jobFile, "application/xml"));
        post.setEntity(me);
        HttpResponse response = httpClient.execute(post);
        String result = EntityUtils.toString(response.getEntity());
        logger.info("Job submitted: " + result);

        return result;
    }

    private String connectToScheduler(String restapi, String username, String password) throws AuthenticationException {
        // Authenticate to the Scheduler
        try {
            String endpoint = restapi + "/scheduler/login";
            HttpPost post = new HttpPost(endpoint);
            post.addHeader("Content-type", "application/x-www-form-urlencoded");
            post.setEntity(new StringEntity("username=" + username + "&password=" + password, "UTF-8"));
            HttpResponse response = httpClient.execute(post);
            String sessid = EntityUtils.toString(response.getEntity());
            logger.debug("Scheduler Session ID: " + sessid);

            return sessid;

        } catch (Throwable e) {
            throw new AuthenticationException("Failed authenticating to the Scheduler", e);
        }
    }

    private String showSorted(Map<String, String> map) {
        return new TreeMap<String, String>(map).toString();
    }
}
