package org.ow2.proactive.brokering;

import org.apache.log4j.Logger;

import java.io.File;

public class Rules extends Scripts {

    static {
        logger = Logger.getLogger(Rules.class.getName());
    }

    public Rules(File path, long refreshMs) {
        super(path, refreshMs);
    }

}
