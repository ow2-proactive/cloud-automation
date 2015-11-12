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
import java.util.Properties;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;


public class Main {

    public static void main(String[] args) throws Exception {
        Server server = createHttpServer(8081);
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

    private static Server createHttpServer(int restPort) {
        Server server = new Server();

        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecureScheme("https");
        httpConfig.setSecurePort(restPort);
        httpConfig.setSendServerVersion(false);
        httpConfig.setSendDateHeader(false);

        org.eclipse.jetty.server.ConnectionFactory[] connectionFactories;

            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath("./Config/keystore");
            sslContextFactory.setKeyStorePassword("activeeon");

            HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
            httpsConfig.addCustomizer(new SecureRequestCustomizer());

            connectionFactories = new org.eclipse.jetty.server.ConnectionFactory[]{
                    new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                    new HttpConnectionFactory(httpsConfig)
            };


        ServerConnector http = new ServerConnector(server, connectionFactories);
        http.setPort(restPort);
        server.addConnector(http);

        return server;
    }

}
