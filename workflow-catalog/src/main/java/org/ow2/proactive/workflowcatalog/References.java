package org.ow2.proactive.workflowcatalog;

import java.util.ArrayList;

public class References extends ArrayList<Reference> {

    public boolean areAllSubmitted() {
        boolean submitted = true;
        for (Reference r : this)
            submitted = submitted && r.isSuccessfullySubmitted();
        return submitted;
    }

    public String getSummary() {
        StringBuffer summary = new StringBuffer();
        for (Reference r : this)
            summary.append(r.toString() + "\n");
        return summary.toString();
    }


}
