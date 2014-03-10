package org.ow2.proactive.workflowcatalog.cli.cmd;

import java.io.InputStream;

public class WcImodeCommand extends AbstractIModeCommand implements Command {

    @Override
    protected InputStream script() {
        return getClass().getResourceAsStream("/RestfulWcActions.groovy");
    }

}
