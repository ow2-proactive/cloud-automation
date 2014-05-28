package org.ow2.proactive.brokering.occi;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ow2.proactive.brokering.Broker;
import org.ow2.proactive.workflowcatalog.References;

public class Resource {
    public static final String OP_CREATE = "create";
    public static final String OP_READ = "read";
    public static final String OP_UPDATE = "update";
    public static final String OP_DELETE = "delete";

    private String uuid;
    private String category;
    private Map<String, String> attributes;

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

    public URL getUrl() {
        try {
            return new URL(OcciServer.getPrefixUrl() + category + "/" + uuid);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create url", e);
        }
    }

    public References create() throws Exception {
        return Broker.getInstance().request(category, OP_CREATE, getAttributes());
    }

    public References read() throws Exception {
        return Broker.getInstance().request(category, OP_READ, getAttributes());
    }

    public References update(String action) throws Exception {
        return Broker.getInstance().request(category, OP_UPDATE, action, getAttributes());
    }

    public References delete() throws Exception {
        return Broker.getInstance().request(category, OP_DELETE, getAttributes());
    }

    public List<Action> getLinks() {
        List<String> actionTitles = Broker.getInstance().listPossibleActions(category, getAttributes());
        List<Action> actions = new ArrayList<Action>();
        for (String actionTitle : actionTitles) {
            actions.add(new Action(actionTitle));
        }
        return actions;
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

}
