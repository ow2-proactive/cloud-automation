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


package org.ow2.proactive.workflowcatalog.forwarder;

import org.apache.http.client.params.ClientPNames;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.ow2.proactive.workflowcatalog.api.utils.ConfigurationHelper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.net.URI;

public class SchedulerForwarder extends ProxyServlet {

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        try {
            targetUriObj = new URI(ConfigurationHelper.getConfiguration().scheduler.url);
        } catch (Exception e) {
            throw new RuntimeException("Trying to read scheduler URL: " + e, e);
        }
        targetUri = targetUriObj.toString();

        HttpParams hcParams = new BasicHttpParams();
        readConfigParam(hcParams, ClientPNames.HANDLE_REDIRECTS, Boolean.class);
        proxyClient = createHttpClient(hcParams);
    }
}
