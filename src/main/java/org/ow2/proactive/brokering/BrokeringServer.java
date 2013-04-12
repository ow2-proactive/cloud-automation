package org.ow2.proactive.brokering;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;


public class BrokeringServer implements Broker {
    private static final Logger logger = Logger.getLogger(Broker.class.getName());
    private String schedulerUrl;
    private String schedulerUsername;
    private String schedulerPassword;
    private Timer timer;
    private Catalog catalog;
    private Rules rules;
    private HttpClient httpClient;

    public BrokeringServer() {
        try {
            File configFile = new File("config/configuration.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Configuration config = (Configuration) jaxbUnmarshaller.unmarshal(configFile);

            schedulerUrl = config.scheduler.url;
            schedulerUsername = config.scheduler.username;
            schedulerPassword = config.scheduler.password;

            httpClient = new DefaultHttpClient();

            catalog = new Catalog(new File(config.catalog.path));
            rules = new Rules(new File(config.rules.path));

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Catalog getCatalog() {
        return null;
    }

    @Override
    public Rules getRules() {
        return null;
    }

    @Override
    public Response request(Attributes attributes) {
        logger.fine("Attributes=" + attributes);

        try {

            File jobFile = catalog.getWorkflow(attributes, rules);
            if (jobFile == null) {
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN_TYPE).entity("No workflow found matching given request").build();
            }
            System.out.println("jobFile = " + jobFile.getAbsolutePath());

            submitToScheduler(jobFile);

        } catch (Throwable e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN_TYPE).entity("Internal Server Error : " + e.getMessage()).build();
        }

        return Response.status(Response.Status.ACCEPTED).build();
    }

    @Override
    public Response request(String action, Attributes attributes) {
        attributes.put("action", action);
        return request(attributes);
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    //
    //                  PRIVATE METHODS
    //
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    private String submitToScheduler(File jobFile) throws AuthenticationException, IOException {
        // Submit selected workflow to the Scheduler
        String restapi = "http://localhost:8080/rest/rest";
        String sessid = connectToScheduler(restapi, "admin", "admin");
        String endpoint = restapi + "/scheduler/submit";
        HttpPost post = new HttpPost(endpoint);
        post.addHeader("sessionid", sessid);
        MultipartEntity me = new MultipartEntity();
        me.addPart("descriptor", new FileBody(jobFile, "application/xml"));
        post.setEntity(me);
        HttpResponse response = httpClient.execute(post);
        String result = EntityUtils.toString(response.getEntity());
        logger.fine("Job submitted: " + result);

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
            logger.fine("Scheduler Session ID: " + sessid);

            return sessid;

        } catch (Throwable e) {
            throw new AuthenticationException("Failed authenticating to the Scheduler", e);
        }
    }
}
