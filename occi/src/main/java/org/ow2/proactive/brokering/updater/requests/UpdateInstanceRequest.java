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


package org.ow2.proactive.brokering.updater.requests;

import org.ow2.proactive.brokering.occi.categories.Utils;

import javax.json.JsonObject;
import java.util.Map;

public class UpdateInstanceRequest extends UpdaterRequest {

    private String category;
    private String uuid;
    private String action;
    private Map<String, String> attributes;

    public UpdateInstanceRequest() {}

    public UpdateInstanceRequest(JsonObject data) {
        category = getString(data, CATEGORY_KEY);
        uuid = getString(data, ID_KEY);
        action = getString(data, ACTION_KEY);
        attributes = Utils.convertToMap(getJsonObject(data, ATTRIBUTES_KEY));
    }

    private String getString(JsonObject obj, String key) {
        checkKeyExists(obj, key);
        try {
            return obj.getString(key);
        } catch (ClassCastException e) {
            return Integer.toString(obj.getInt(key));
        }
    }

    private JsonObject getJsonObject(JsonObject obj, String key) {
        checkKeyExists(obj, key);
        return obj.getJsonObject(key);
    }

    private void checkKeyExists(JsonObject obj, String key) {
        if (!obj.containsKey(key))
            throw new RuntimeException(
                    "Could not find key '" + key + "' in JSON '" + obj.toString() + "'");
    }

    public String getAction() {
        return action;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getUuid() {
        return uuid;
    }

    public String getCategory() {
        return category;
    }

    public void processSpecialAttributes(String locationUrl) {
        for (String key: attributes.keySet()) {
            String value = attributes.get(key);
            if (UpdaterRequest.LOCATION_OF_PARENT_KEY.equals(value)) {
                attributes.put(key, locationUrl);
            }
        }
    }

}
