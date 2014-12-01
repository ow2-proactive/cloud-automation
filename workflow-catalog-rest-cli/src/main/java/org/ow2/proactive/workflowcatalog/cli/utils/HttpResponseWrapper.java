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
