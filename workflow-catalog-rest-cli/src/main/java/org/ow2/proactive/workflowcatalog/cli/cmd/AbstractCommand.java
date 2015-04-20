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



package org.ow2.proactive.workflowcatalog.cli.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Stack;

import org.ow2.proactive.workflowcatalog.api.exceptions.ExceptionBean;
import org.ow2.proactive.workflowcatalog.cli.ApplicationContext;
import org.ow2.proactive.workflowcatalog.cli.CLIException;
import org.ow2.proactive.workflowcatalog.cli.HttpResponseStatus;
import org.ow2.proactive.workflowcatalog.cli.utils.HttpResponseWrapper;
import org.ow2.proactive.workflowcatalog.cli.utils.HttpUtility;
import org.ow2.proactive.workflowcatalog.cli.utils.StringUtility;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;

import static org.ow2.proactive.workflowcatalog.cli.CLIException.REASON_IO_ERROR;
import static org.ow2.proactive.workflowcatalog.cli.CLIException.REASON_UNAUTHORIZED_ACCESS;
import static org.ow2.proactive.workflowcatalog.cli.HttpResponseStatus.FORBIDDEN;


public abstract class AbstractCommand implements Command {

    protected int statusCode(HttpResponseStatus status) {
        return status.statusCode();
    }

    protected int statusCode(HttpResponseWrapper response) {
        return response.getStatusCode();
    }

    protected void writeLine(ApplicationContext currentContext, String format,
      Object... args) {
        if (!currentContext.isSilent()) {
            try {
                currentContext.getDevice().writeLine(format, args);
            } catch (IOException ioe) {
                throw new CLIException(REASON_IO_ERROR, ioe);
            }
        }
    }

    protected String readLine(ApplicationContext currentContext, String format,
      Object... args) {
        try {
            return currentContext.getDevice().readLine(format, args);
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }
    }

    protected char[] readPassword(ApplicationContext currentContext,
      String format, Object... args) {
        try {
            return currentContext.getDevice().readPassword(format, args);
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }
    }

    protected HttpResponseWrapper execute(HttpUriRequest request,
      ApplicationContext currentContext) {
        String sessionId = currentContext.getSessionId();
        if (sessionId != null) {
            request.setHeader("sessionid", sessionId);
        }
        HttpClient client = HttpUtility.threadSafeClient();
        try {
            if ("https".equals(request.getURI().getScheme())
              && currentContext.canInsecureAccess()) {
                HttpUtility.setInsecureAccess(client);
            }
            HttpResponse response = client.execute(request);
            return new HttpResponseWrapper(response);

        } catch (Exception e) {
            throw new CLIException(CLIException.REASON_OTHER, e);
        } finally {
            ((HttpRequestBase) request).releaseConnection();
        }
    }

    @SuppressWarnings("unchecked")
    protected void handleError(String errorMessage,
      HttpResponseWrapper response, ApplicationContext currentContext) {
        String responseContent = StringUtility.responseAsString(response);
        Stack resultStack = resultStack(currentContext);
        ExceptionBean errorView = null;
        try {
            errorView = currentContext.getObjectMapper().readValue(
              responseContent.getBytes(), ExceptionBean.class);
            resultStack.push(errorView);

        } catch (Throwable error) {
            resultStack.push(responseContent);
            // if an ErrorView object can't be built from the response. Hence
            // process the response as a string
        }
        if (errorView != null) {
            writeError(errorMessage, errorView, currentContext, response.getStatusCode());
        } else {
            writeError(errorMessage, responseContent, currentContext);
        }
    }

    @SuppressWarnings("unchecked")
    protected void handleError(String errorMessage,
      Exception error, ApplicationContext currentContext) {
        Stack resultStack = resultStack(currentContext);
        resultStack.push(error);

        /*
        if (error instanceof NotConnectedRestException) {
            throw new CLIException(REASON_UNAUTHORIZED_ACCESS, errorMessage,
                    error);
        }
        */

        writeLine(currentContext, errorMessage);
        Throwable cause = error.getCause();

        writeLine(currentContext, "%nError Message: %s",
          (cause == null) ? error.getMessage() : cause.getMessage());

        writeLine(currentContext, "%nStack Trace: %s",
                StringUtility.stackTraceAsString((cause == null) ? error
                        : cause));
    }

    private void writeError(String errorMsg, String responseContent,
      ApplicationContext currentContext) {
        writeLine(currentContext, errorMsg);

        String errorMessage = null, errorCode = null;
        BufferedReader reader = new BufferedReader(new StringReader(
          responseContent));

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("errorMessage:")) {
                    errorMessage = line.substring(line.indexOf(':')).trim();
                    break;
                }
            }

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("httpErrorCode:")) {
                    errorCode = line.substring(line.indexOf(':')).trim();
                    break;
                }
            }
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }

        if (errorCode != null) {
            writeLine(currentContext, "%s %s", "HTTP Error Code:", errorCode);
        }

        if (errorMessage != null) {
            writeLine(currentContext, "%s %s", "Error Message:", errorMessage);
        }

        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("stackTrace:")) {
                    while ((line = reader.readLine()) != null) {
                        writeLine(currentContext, line);
                    }
                    break;
                }
            }
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }

        if (errorCode == null && errorMessage == null) {
            writeLine(currentContext, "%s%n%s", "Error Message:",
              responseContent);
        }
    }

    public Stack resultStack(ApplicationContext currentContext) {
        return currentContext.resultStack();
    }

    private void writeError(String errorMessage, ExceptionBean error,
      ApplicationContext currentContext, int httpErrorCode) {
        if (statusCode(FORBIDDEN) == httpErrorCode) {
            // this exception would be handled at an upper level ..
            throw new CLIException(REASON_UNAUTHORIZED_ACCESS,
              error.getExceptionMessage());
        }
        writeLine(currentContext, errorMessage);
        writeLine(currentContext, "%s %s", "HTTP Error Code:",
          httpErrorCode);
        writeLine(currentContext, "%s %s", "Error Message:",
          error.getExceptionMessage());

        writeLine(currentContext, "%s%n%s", "Stack Trace:",
          stackTraceToString(error.getThrowable()));
    }

    private String stackTraceToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
