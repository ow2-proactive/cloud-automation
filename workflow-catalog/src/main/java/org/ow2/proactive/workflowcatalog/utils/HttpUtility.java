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


package org.ow2.proactive.workflowcatalog.utils;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class HttpUtility {

    private HttpUtility() {
    }

    public static HttpClient turnClientIntoInsecure(HttpClient client)
            throws SecurityException {

        try {
            SSLSocketFactory socketFactory = new SSLSocketFactory(
                    new RelaxedTrustStrategy(),
                    SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            Scheme https = new Scheme("https", 443, socketFactory);
            client.getConnectionManager().getSchemeRegistry().register(https);
            return client;
        } catch (KeyManagementException e) {
            throw new SecurityException(e);
        } catch (UnrecoverableKeyException e) {
            throw new SecurityException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        } catch (KeyStoreException e) {
            throw new SecurityException(e);
        }

    }

    private static class RelaxedTrustStrategy implements TrustStrategy {
        @Override
        public boolean isTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
            return true;
        }
    }

    public static boolean isNotSuccessStatusCode(int statusCode) {
        return (((statusCode < 200) || (statusCode > 299)));
    }

}
