package org.ow2.proactive.brokering.triggering;

import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.Scripts;

import java.io.File;

public class Actions extends Scripts {

    static {
        logger = Logger.getLogger(Actions.class.getName());
    }

    public Actions(File path, long refreshMs) {
        super(path, refreshMs);
    }

}
