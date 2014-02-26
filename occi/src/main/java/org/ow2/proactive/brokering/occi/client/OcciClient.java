package org.ow2.proactive.brokering.occi.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.occi.categories.Utils;
import org.ow2.proactive.workflowcatalog.utils.HttpUtility;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OcciClient {

    private static final Logger logger = Logger.getLogger(OcciClient.class.getName());

    private URI endpoint;
    private HttpClient httpClient;

    public OcciClient(String endpoint) throws URISyntaxException {
        this.endpoint = new URI(endpoint);
        httpClient = HttpUtility.turnClientIntoInsecure(new DefaultHttpClient());
    }

    public ResourceInstance createResource(String category, Map<String, String> attributes) throws ResourceCreationException {
        return createResource(category, attributes, null);
    }

    public ResourceInstance createResource(String category, Map<String, String> attributes, String action) throws ResourceCreationException {
        String url = generateUrl(endpoint + "/" + category, action);

        HttpPost post = new HttpPost(url);
        post.setHeader("X-OCCI-Attribute", Utils.buildString(attributes));

        try {
            HttpResponse response = httpClient.execute(post);
            if (isSuccessful(response)) {
                String resourceUrlRaw = getEntityAsString(response);
                return new ResourceInstance(resourceUrlRaw);
            } else {
                throw new InternalError(getEntityAsString(response));
            }
        } catch (Exception e) {
            throw new ResourceCreationException(e);
        }
    }

    public ResourceInstance updateResource(String category, String uuid, Map<String, String> attributes) throws ResourceCreationException {
        return updateResource(category, uuid, attributes, null);
    }

    public ResourceInstance updateResource(String category, String uuid, Map<String, String> attributes, String action) throws ResourceCreationException {
        String url = generateUrl(endpoint + "/" + category + "/" + uuid, action);

        HttpPut put = new HttpPut(url);
        put.setHeader("X-OCCI-Attribute", Utils.buildString(attributes));

        try {
            HttpResponse response = httpClient.execute(put);
            if (isSuccessful(response)) {
                String resourceUrlRaw = getEntityAsString(response);
                return new ResourceInstance(resourceUrlRaw);
            } else {
                throw new InternalError(getEntityAsString(response));
            }
        } catch (Exception e) {
            throw new ResourceCreationException(e);
        }
    }

    public ResourceInstance getResource(String category, String uuid) throws ResourceReadingException {
        HttpGet get = new HttpGet(endpoint + "/" + category + "/" + uuid);
        try {
            HttpResponse response = httpClient.execute(get);
            if (isSuccessful(response)) {
                String propertiesRaw = getEntityAsString(response);
                return new ResourceInstance(parsePropertiesRaw(propertiesRaw));
            } else {
                throw new InternalError(getEntityAsString(response));
            }
        } catch (Exception e) {
            throw new ResourceReadingException(e);
        }
    }

    private String generateUrl(String path, String action) {
        return path + (action != null ? "?action=" + action : "");
    }

    private String getEntityAsString(HttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity());
    }

    private boolean isSuccessful(HttpResponse resp) {
        int code = resp.getStatusLine().getStatusCode();
        return (code > 199 && code < 300);
    }

    private Map<String, String> parsePropertiesRaw(String propertiesRaw) throws ResourceReadingException {
        Map<String, String> map = new HashMap<String, String>();
        Pattern propertyRegex = Pattern.compile("X-OCCI-Attribute: (.*)=\"(.*)\"");
        for (String line: propertiesRaw.split("\n")) {
            Matcher m = propertyRegex.matcher(line.trim());
            if(m.find())
                map.put(m.group(1), m.group(2));
            else
                logger.warn("Invalid line: " + line);
        }
        return map;
    }

    public static void main(String args[]) throws Exception {
        OcciClient client = new OcciClient("http://localhost:8081/occi/api/occi");

        Map<String, String> ar = new HashMap<String, String>();
        //ar.put("rule","numergy");
        //ar.put("provider","numergy");
        //ar.put("numergy.vm.name", "vmname-aftermidi");
        //ar.put("application", "elasticsearch");
        ar.put("occi.paas.status", "down");

        ResourceInstance base = client.updateResource("platform", "a722403e-9b33-4cdf-82e6-ab6aa6a6da7f", Collections.EMPTY_MAP, "stop");
        ResourceInstance trigger = client.updateResource("actiontrigger", "c31aa092-df68-4d17-a44d-82df6ced03d0", Collections.EMPTY_MAP, "stop");

        Map<String, String> ar1 = new HashMap<String, String>();
        ar1.put("occi.paas.status", "down");
        ResourceInstance platformbig = client.updateResource("platform", "c6dff68a-66e7-46bf-8f65-a0731e6158f2", ar1);
        //System.out.println(base);
        //System.out.println(trigger);

    }

}
