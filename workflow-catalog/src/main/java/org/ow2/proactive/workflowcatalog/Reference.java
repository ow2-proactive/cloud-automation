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


package org.ow2.proactive.workflowcatalog;

import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;

public class Reference {

    private SubmissionStatus submissionStatus;
    private String submissionId;
    private String submissionMessage;
    private Nature natureOfReference;

    public Reference(Nature natureOfReference, SubmissionStatus submissionStatus, String message, String id) {
        this.natureOfReference = natureOfReference;
        this.submissionStatus = submissionStatus;
        this.submissionMessage = message;
        this.submissionId = id;
    }

    public static Reference buildActionTriggerReference(String message, String id) {
        return new Reference(Nature.NATURE_OTHER, SubmissionStatus.SUBMISSION_DONE, message, id);
    }

    public static Reference buildActionTriggerFailedReference(String message, Exception e) {
        return new Reference(Nature.NATURE_OTHER, SubmissionStatus.SUBMISSION_NOT_DONE, message + ":" + e.getMessage(), null);
    }


    public static Reference buildJobFailedReference(String json) {
        return new Reference(Nature.NATURE_JOB, SubmissionStatus.SUBMISSION_NOT_DONE, json, null);
    }

    public static Reference buildJobReference(String info, JobIdData response) {
        String message = info + ":" + response.getReadableName();
        String id = null;
        SubmissionStatus status;
        try {
            id = Long.toString(response.getId());
            status = SubmissionStatus.SUBMISSION_DONE;
        } catch (Exception e) {
            status = SubmissionStatus.SUBMISSION_NOT_DONE;
        }
        return new Reference(Nature.NATURE_JOB, status, message, id);
    }

    public String getId() {
        return submissionId;
    }

    public Boolean isSuccessfullySubmitted() {
        return submissionStatus == SubmissionStatus.SUBMISSION_DONE;
    }

    public SubmissionStatus getSubmissionStatus() {
        return submissionStatus;
    }

    public String getSubmissionMessage() {
        return submissionMessage;
    }

    public Nature getNatureOfReference() {
        return natureOfReference;
    }

    public String toString() {
        return
                "[Reference nature: '" + natureOfReference +
                        "', submitted: '" + submissionStatus +
                        "', id: '" + submissionId +
                        "', submissionMessage: '" + submissionMessage +
                        "']";
    }

    public static enum Nature {

        NATURE_UNKNOWN(0), NATURE_JOB(1), NATURE_OTHER(2);

        private int nature;

        private Nature(int nature) {
            this.nature = nature;
        }

    }

    public static enum SubmissionStatus {

        SUBMISSION_UNKNOWN(0), SUBMISSION_DONE(1), SUBMISSION_NOT_DONE(2);

        private int submissionStatus;

        private SubmissionStatus(int submissionStatus) {
            this.submissionStatus = submissionStatus;
        }

    }
}
