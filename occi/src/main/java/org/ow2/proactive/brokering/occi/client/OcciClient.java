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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OcciClient {

    private static final Logger logger = Logger.getLogger(OcciClient.class.getName());

    private URI endpoint;
    private HttpClient httpClient;

    public OcciClient(String endpoint) throws URISyntaxException {
        this.endpoint = new URI(normalize(endpoint));
        this.httpClient = HttpUtility.turnClientIntoInsecure(new DefaultHttpClient());
    }

    public ResourceInstance createResource(String category, Map<String, String> attributes) throws ResourceCreationException, ResourceReadingException {
        return createResource(category, attributes, null);
    }

    public ResourceInstance createResource(String category, Map<String, String> attributes, String action) throws ResourceCreationException, ResourceReadingException {
        String urlRaw = internalCreateResource(category, attributes, action);
        return new ResourceInstance(urlRaw).refresh(this);
    }

    private String internalCreateResource(String category, Map<String, String> attributes, String action) throws ResourceCreationException {
        String url = generateUrl(endpoint + "/" + category, action);

        HttpPost post = new HttpPost(url);
        post.setHeader("X-OCCI-Attribute", Utils.buildString(attributes));

        try {
            HttpResponse response = httpClient.execute(post);
            if (isSuccessful(response)) {
                String resourceUrlRaw = getEntityAsString(response);
                return resourceUrlRaw;
            } else {
                throw new InternalError(getEntityAsString(response));
            }
        } catch (Exception e) {
            throw new ResourceCreationException(e);
        }
    }

    public ResourceInstance updateResource(ResourceInstance resource, Map<String, String> attributes) throws ResourceCreationException, ResourceReadingException {
        return updateResource(resource.getCategory(), resource.getUuid(), attributes, null);
    }

    public ResourceInstance updateResource(String category, String uuid, Map<String, String> attributes) throws ResourceCreationException, ResourceReadingException {
        return updateResource(category, uuid, attributes, null);
    }

    public ResourceInstance updateResource(ResourceInstance resource, Map<String, String> attributes, String action) throws ResourceCreationException, ResourceReadingException {
        return updateResource(resource.getCategory(), resource.getUuid(), attributes, action);
    }

    public ResourceInstance updateResource(String category, String uuid, Map<String, String> attributes, String action) throws ResourceCreationException, ResourceReadingException {
        String urlRaw = internalUpdateResource(category, uuid, attributes, action);
        return new ResourceInstance(urlRaw).refresh(this);
    }

    private String internalUpdateResource(String category, String uuid, Map<String, String> attributes, String action) throws ResourceCreationException {
        String url = generateUrl(endpoint + "/" + category + "/" + uuid, action);

        HttpPut put = new HttpPut(url);
        put.setHeader("X-OCCI-Attribute", Utils.buildString(attributes));

        try {
            HttpResponse response = httpClient.execute(put);
            if (isSuccessful(response)) {
                String resourceUrlRaw = getEntityAsString(response);
                return resourceUrlRaw;
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

    private String normalize(String endpoint) {
        if (endpoint.endsWith("/"))
            return endpoint.substring(0, endpoint.length() - 1);
        else
            return endpoint;

    }

}
