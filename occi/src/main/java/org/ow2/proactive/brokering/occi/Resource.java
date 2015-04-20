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


package org.ow2.proactive.brokering.occi;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;


public class Resource {
    public static final String OP_CREATE = "create";
    public static final String OP_UPDATE = "update";

    private static Logger logger = Logger.getLogger(Resource.class);

    private String uuid;
    private String category;
    private Map<String, String> attributes;
    private List<Action> actions;
    private List<Resource> links;

    public Resource() {}

    public Resource(String uuid, String category, Map<String, String> attributes) {
        this.uuid = uuid;
        this.category = category;
        this.attributes = attributes;
    }

    public String getUuid() {
        return uuid;
    }

    public String getCategory() {
        return category;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getRelativePath() {
        return "/" + category + "/" + uuid;
    }

    public String getFullPath(String prefixUrl) {
        try {
            return new URL(prefixUrl + getRelativePath()).toString();
        } catch (MalformedURLException e) {
            logger.warn("Server url is not an URL", e);
            return getRelativePath();
        }
    }

    public String toString() {
        String result = "";
        TreeMap<String, String> map = new TreeMap<String, String>(attributes);
        for (String key : map.keySet()) {
            if (map.get(key) != null) {
                result += "X-OCCI-Attribute: " + key + "=\"" + map.get(key) + "\"\n";
            }
        }
        return result;
    }

    public boolean equals(Resource target) {
        if (!this.getUuid().equals(target.uuid))
            return false;

        if (!this.getCategory().equals(target.category))
            return false;

        if (this.getAttributes().size() != target.getAttributes().size())
            return false;

        for (String key: this.getAttributes().keySet()) {
            if (!target.getAttributes().containsKey(key))
                return false;
            String value = this.getAttributes().get(key);
            String valueTarget = target.getAttributes().get(key);
            if (!value.equals(valueTarget))
                return false;
        }

        return true;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> links) {
        this.actions = links;
    }

    public List<Resource> getLinks() {
        return links;
    }

    public void setLinks(List<Resource> links) {
        this.links = links;
    }
}
