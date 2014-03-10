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
        StringBuilder summary = new StringBuilder();
        if (this.isEmpty()) {
            summary.append("No references");
        } else {
            for (Reference r : this) {
                summary.append(r.toString());
                summary.append("\n");
            }
        }
        return summary.toString();
    }


}
