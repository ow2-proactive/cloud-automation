package org.ow2.proactive.brokering.triggering;

import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.Scripts;

import java.io.File;

public class Conditions extends Scripts {

    static {
        logger = Logger.getLogger(Conditions.class.getName());
    }

    public Conditions(File path, long refreshMs) {
        super(path, refreshMs);
    }

}
