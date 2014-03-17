package org.ow2.proactive.workflowcatalog.api.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AllExceptionsMapper implements ExceptionMapper<Exception> {
    public Response toResponse(Exception exception) {
        return ExceptionFormatterUtils.createResponse(exception);
    }
}

