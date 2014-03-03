package org.ow2.proactive.brokering;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "configuration")
public class Configuration {
    @XmlElement
    public Scheduler scheduler;

    @XmlElement
    public Catalog catalog;

    @XmlElement
    public Rules rules;

    @XmlElement
    public Actions actions;

    @XmlElement
    public Conditions conditions;

    @XmlElement
    public Security security;

    @XmlElement
    public OcciUpdater updater;

    @XmlElement
    public Server server;

    @Override
    public String toString() {
        String result = "Scheduler : " + scheduler.url + " (" + scheduler.username + " / " + scheduler.password + ")\n";
        result += "Catalog : " + catalog.path + "\n";
        result += "Actions : " + actions.path + "\n";
        result += "Conditions : " + conditions.path + "\n";
        result += "Rules : " + rules.path + "\n";
        result += "Security : " + security.insecuremode + "\n";
        return result;
    }

    public static class Security {
        @XmlAttribute
        public Boolean insecuremode;
    }

    public static class Scheduler {
        @XmlAttribute
        public String username;

        @XmlAttribute
        public String password;

        @XmlAttribute
        public String url;
    }

    public static class Catalog {
        @XmlAttribute
        public String path;

        @XmlAttribute
        public long refresh;
    }

    public static class Rules {
        @XmlAttribute
        public String path;

        @XmlAttribute
        public long refresh;
    }

    public static class Actions {
        @XmlAttribute
        public String path;

        @XmlAttribute
        public long refresh;
    }

    public static class Conditions {
        @XmlAttribute
        public String path;

        @XmlAttribute
        public long refresh;
    }

    public static class OcciUpdater {
        @XmlAttribute
        public long refresh;
    }

    public static class Server {
        @XmlAttribute
        public String prefix;
    }
}
