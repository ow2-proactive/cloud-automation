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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceInstance extends HashMap<String, String> {

    private static final String PREFIX = "X-OCCI-Location: ";

    private String location;

    public ResourceInstance(Map<String, String> args) {
        this.putAll(args);
    }

    public ResourceInstance(String location) {
        if (location.contains(PREFIX))
            this.location = extractLocation(location);
        else
            this.location = location.trim();
    }

    public ResourceInstance refresh(OcciClient client) throws ResourceReadingException {
        ResourceInstance resource = client.getResource(this.getCategory(), this.getUuid());
        this.clear();
        this.putAll(resource);
        return this;
    }

    public ResourceInstance update(OcciClient client, Map<String, String> properties, String action) throws ResourceReadingException, ResourceCreationException {
        ResourceInstance resource = client.updateResource(this.getCategory(), this.getUuid(), properties, action);
        this.clear();
        this.putAll(resource);
        return this;
    }

    public String getLocation() {
        if (location != null) {
            return location;
        } else {
            throw new RuntimeException("No location is set");
        }
    }

    public String getCategory() {
        if (location != null) {
            return extractCategory(location);
        } else {
            throw new RuntimeException("No location is set");
        }
    }

    public String getUuid() {
        if (containsKey("occi.core.id")) {
            return get("occi.core.id");
        } else if (location != null) {
            return extractUuid(location);
        } else {
            throw new RuntimeException("No occi.core.id or location are set");
        }
    }

    private String extractCategory(String resourceLocationUrl) {
        Pattern propertyRegex = Pattern.compile("/([a-zA-Z]*)/[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
        Matcher m = propertyRegex.matcher(resourceLocationUrl);
        if(m.find())
            return m.group(1);
        else
            throw new RuntimeException("Cannot extract category: " + resourceLocationUrl);
    }

    private String extractUuid(String resourceLocationUrl) {
        Pattern propertyRegex = Pattern.compile("([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})");
        Matcher m = propertyRegex.matcher(resourceLocationUrl);
        if(m.find())
            return m.group(1);
        else
            throw new RuntimeException("Cannot extract UUID: " + resourceLocationUrl);
    }

    private String extractLocation(String resourceLocationRaw) {
        Pattern propertyRegex = Pattern.compile(PREFIX + "(.*)");
        Matcher m = propertyRegex.matcher(resourceLocationRaw);
        if(m.find())
            return m.group(1);
        else
            throw new RuntimeException("Cannot extract location: " + resourceLocationRaw);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Location: ");
        builder.append(location);
        builder.append(" -> [\n");
        for (String key: super.keySet()) {
            builder.append("   ");
            builder.append(key);
            builder.append(":");
            builder.append(super.get(key));
            builder.append("\n");
        }
        builder.append("]\n");
        return builder.toString();
    }

}
