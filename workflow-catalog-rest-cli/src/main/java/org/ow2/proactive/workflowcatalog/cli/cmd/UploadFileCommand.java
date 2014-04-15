/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
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
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive.workflowcatalog.cli.cmd;

import java.io.FileInputStream;
import org.ow2.proactive.workflowcatalog.cli.CLIException;
import org.ow2.proactive.workflowcatalog.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerRestClient;

import static org.apache.commons.io.IOUtils.closeQuietly;

public class UploadFileCommand extends AbstractCommand implements Command {

    private String spaceName;
    private String filePath;
    private String fileName;
    private String localFile;

    public UploadFileCommand(
            String srcFilePath, String dstSpaceName,
            String dstFilePath, String dstFileName) {
        this.localFile = srcFilePath;
        this.spaceName = dstSpaceName;
        this.filePath = dstFilePath;
        this.fileName = dstFileName;
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        SchedulerRestClient client = currentContext.getSchedulerClient();
        FileInputStream fileStream = null;
        try {
            fileStream = new FileInputStream(localFile);
            boolean uploaded = client.pushFile(
                    currentContext.getSessionId(), spaceName, filePath, fileName, fileStream);
            resultStack(currentContext).push(uploaded);
            if (uploaded) {
                writeLine(currentContext, "'%s' successfully uploaded to '%s:%s%s'",
                          localFile,
                          spaceName,
                          filePath,
                          fileName);
            } else {
                writeLine(currentContext, "Cannot upload the file '%s'", localFile);
            }
        } catch (Exception error) {
            if (fileStream != null) {
                closeQuietly(fileStream);
            }
            handleError(
                    String.format("An error occurred when uploading the file '%s'", localFile),
                    error, currentContext);
        }
    }

}
