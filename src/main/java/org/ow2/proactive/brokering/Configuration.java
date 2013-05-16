package org.ow2.proactive.brokering;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "configuration")
public class Configuration {
    @XmlElement
    Scheduler scheduler;

    @XmlElement
    Catalog catalog;

    @XmlElement
    Rules rules;

    @Override
    public String toString() {
        String result = "Scheduler : " + scheduler.url + " (" + scheduler.username + " / " + scheduler.password + ")\n";
        result += "Catalog : " + catalog.path + "\n";
        result += "Rules : " + rules.path;

        return result;
    }

    static class Scheduler {
        @XmlAttribute
        String username;

        @XmlAttribute
        String password;

        @XmlAttribute
        String url;
    }

    static class Catalog {
        @XmlAttribute
        String path;

        @XmlAttribute
        long refresh;
    }

    static class Rules {
        @XmlAttribute
        String path;

        @XmlAttribute
        long refresh;
    }
}
