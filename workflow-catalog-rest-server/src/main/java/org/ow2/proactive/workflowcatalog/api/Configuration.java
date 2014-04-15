package org.ow2.proactive.workflowcatalog.api;

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
    public Security security;

    @Override
    public String toString() {
        String result = "Scheduler : " + scheduler.url + "\n";
        result += "Catalog : " + catalog.path + "\n";
        result += "Security : " + security.insecuremode + "\n";
        return result;
    }

    public static class Security {
        @XmlAttribute
        public Boolean insecuremode;
    }

    public static class Scheduler {
        @XmlAttribute
        public String url;
    }

    public static class Catalog {
        @XmlAttribute
        public String path;

        @XmlAttribute
        public long refresh;
    }

}
