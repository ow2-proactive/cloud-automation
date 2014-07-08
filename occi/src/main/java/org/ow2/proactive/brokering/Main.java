package org.ow2.proactive.brokering;

import java.io.File;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;


public class Main {

    public static void main(String[] args) throws Exception {
        Server server = createHttpsServer(8081, 8443);
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

    private static Server createHttpsServer(int httpPort, int httpsPort) {
        Server server = new Server();

        SslSocketConnector https = new SslSocketConnector();
        https.setKeystore(Main.class.getResource("/keystore").toString());
        https.setKeyPassword("activeeon");
        https.setPort(httpsPort);
        server.addConnector(https);

        SelectChannelConnector redirectHttpToHttps = new SelectChannelConnector();
        redirectHttpToHttps.setPort(httpPort);
        redirectHttpToHttps.setConfidentialPort(httpsPort);
        server.addConnector(redirectHttpToHttps);

        return server;
    }

}
