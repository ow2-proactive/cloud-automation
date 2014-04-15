package org.ow2.proactive.workflowcatalog.cli.cmd;

import static org.ow2.proactive.workflowcatalog.cli.HttpResponseStatus.OK;
import static org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM;
import static org.ow2.proactive.workflowcatalog.cli.CLIException.*;

import java.io.File;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.ow2.proactive.workflowcatalog.cli.ApplicationContext;
import org.ow2.proactive.workflowcatalog.cli.CLIException;
import org.ow2.proactive.workflowcatalog.cli.utils.FileUtility;
import org.ow2.proactive.workflowcatalog.cli.utils.HttpResponseWrapper;
import org.ow2.proactive.workflowcatalog.cli.utils.StringUtility;

public class LoginWithCredentialsCommand extends AbstractLoginCommand implements
        Command {
    public static final String CRED_FILE = "org.ow2.proactive_grid_cloud_portal.cli.cmd.LoginWithCredentials.credFile";

    private String pathname;

    public LoginWithCredentialsCommand(String pathname) {
        this.pathname = pathname;
    }

    @Override
    protected String login(ApplicationContext currentContext)
            throws CLIException {
        File credentials = new File(pathname);
        if (!credentials.exists()) {
            throw new CLIException(REASON_INVALID_ARGUMENTS, String.format(
                    "File does not exist: %s", credentials.getAbsolutePath()));
        }
        HttpPost request = new HttpPost(currentContext.getWcResourceUrl("login"));
        MultipartEntity entity = new MultipartEntity();
        entity.addPart("credential",
                new ByteArrayBody(FileUtility.byteArray(credentials),
                        APPLICATION_OCTET_STREAM.getMimeType()));
        request.setEntity(entity);
        HttpResponseWrapper response = execute(request, currentContext);
        if (statusCode(OK) == statusCode(response)) {
            return StringUtility.responseAsString(response).trim();
        } else {
            handleError("An error occurred while logging: ", response,
                    currentContext);
            throw new CLIException(REASON_OTHER,
                    "An error occurred while logging.");
        }
    }

    @Override
    protected String getAlias(ApplicationContext currentContext) {
        String pathname = currentContext.getProperty(CRED_FILE, String.class);
        return FileUtility.md5Checksum(new File(pathname));
    }

    @Override
    protected void setAlias(ApplicationContext currentContext) {
        currentContext.setProperty(CRED_FILE, pathname);
    }
}

