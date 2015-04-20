/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 2013-2015 ActiveEon
 * 
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * $$ACTIVEEON_INITIAL_DEV$$
 */


package org.ow2.proactive.brokering;

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.webapp.WebAppContext;


public class Main {

    public static void main(String[] args) throws Exception {
        Server server = createHttpsServer(8081, 8443);
        WebAppContext webApp = new WebAppContext();
        webApp.setParentLoaderPriority(true);
        webApp.setContextPath("/ca");
        webApp.setDescriptor(new File("src/main/webapp/WEB-INF/web.xml").getAbsolutePath());
        webApp.setResourceBase("src/main/webapp");
        server.setHandler(webApp);
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
