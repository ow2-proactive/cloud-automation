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

import org.apache.commons.io.IOUtils;
import org.ow2.proactive.workflowcatalog.cli.ApplicationContext;
import org.ow2.proactive.workflowcatalog.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerRestClient;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobStatusData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;
import static org.ow2.proactive.workflowcatalog.cli.utils.FileUtility.buildOutputStream;

public class GetJobLogsCommand extends AbstractCommand implements Command {

    private String jobId;

    public GetJobLogsCommand(String jobId) {
        this.jobId = jobId;
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        SchedulerRestClient client = currentContext.getSchedulerClient();
        try {

            JobStateData state = client.getScheduler().listJobs(
                    currentContext.getSessionId(), jobId);
            JobStatusData status = state.getJobInfo().getStatus();

            if (status.equals(JobStatusData.FINISHED) ||
                    status.equals(JobStatusData.CANCELED) ||
                    status.equals(JobStatusData.FAILED) ) {

                writeLine(currentContext, "### Server logs: ###\n\n\n");
                String serverLog = client.getScheduler().jobServerLog(currentContext.getSessionId(), jobId);
                writeLine(currentContext, serverLog);

                writeLine(currentContext, "### Job logs: ###\n\n\n");
                InputStream is = client.getScheduler().jobFullLogs(currentContext.getSessionId(), jobId, null);
                String fullLogs = readAndClose(is);
                writeLine(currentContext, fullLogs);

            } else if (status.equals(JobStatusData.RUNNING)) {

                writeLine(currentContext, "### Job logs: ###\n\n\n");
                String str = client.getScheduler().getLiveLogJob(currentContext.getSessionId(), jobId);
                writeLine(currentContext, str);

            } else {

                String str = client.getScheduler().getLiveLogJob(currentContext.getSessionId(), jobId);
                writeLine(currentContext, str);
            }
        } catch (Exception error) {
            handleError("An error occurred while downloading the file " + error, error, currentContext);
        }
    }


    private String readAndClose(InputStream is) {
        try {
            return IOUtils.toString(is, "UTF-8");
        } catch (IOException e) {
            return "<unknown>";
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

}
