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


package org.ow2.proactive.workflowcatalog.cli.utils;

import static org.ow2.proactive.workflowcatalog.cli.CLIException.REASON_IO_ERROR;
import static org.ow2.proactive.workflowcatalog.cli.CLIException.REASON_OTHER;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.ow2.proactive.workflowcatalog.cli.CLIException;

public class HttpResponseWrapper {
    private byte[] buffer = new byte[]{};
    private int statusCode = -1;

    public HttpResponseWrapper(HttpResponse response) throws CLIException {
        statusCode = response.getStatusLine().getStatusCode();
        InputStream inputStream = null;
        try {
            HttpEntity e = response.getEntity();
            if (e != null ) {
                inputStream = e.getContent();
                if (inputStream != null) {
                    buffer = IOUtils.toByteArray(inputStream);
                }
            }
        } catch (IllegalStateException e) {
            throw new CLIException(REASON_OTHER, e);
        } catch (IOException e) {
            throw new CLIException(REASON_IO_ERROR, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public byte[] getContent() {
        return buffer;
    }
}
