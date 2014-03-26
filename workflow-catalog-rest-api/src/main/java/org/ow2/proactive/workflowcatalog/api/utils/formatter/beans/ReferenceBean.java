package org.ow2.proactive.workflowcatalog.api.utils.formatter.beans;

import com.google.gson.Gson;
import org.ow2.proactive.workflowcatalog.Reference;
import static org.ow2.proactive.workflowcatalog.Reference.*;

public class ReferenceBean {

    public String submissionId;
    public String submissionMessage;
    public Nature natureOfReference;
    public SubmissionStatus submissionStatus;

    public ReferenceBean() { }

    public ReferenceBean(Reference reference) {
        this.submissionMessage = reference.getSubmissionMessage();
        this.submissionId = reference.getId();
        this.submissionStatus = reference.getSubmissionStatus();
        this.natureOfReference = reference.getNatureOfReference();
    }

    public Reference generateReference() {
        return new Reference(natureOfReference, submissionStatus, submissionMessage, submissionId);
    }

    public String toString() {
        return new Gson().toJson(this);
    }

}
