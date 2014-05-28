package org.ow2.proactive.brokering;

import java.io.File;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;


public class Main {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8081);
        WebAppContext webApp = new WebAppContext();
        webApp.setParentLoaderPriority(true);
        webApp.setContextPath("/ca");
        webApp.setDescriptor(new File("src/main/webapp/WEB-INF/web.xml").getAbsolutePath());
        webApp.setResourceBase("src/main/webapp");
        server.addHandler(webApp);
        server.start();
        System.out.println("---------- Started -------------");
        server.join();
    }

}
