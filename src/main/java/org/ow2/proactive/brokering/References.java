package org.ow2.proactive.brokering;
import java.util.ArrayList;
public class References extends ArrayList<Reference> {

    public boolean areAllSubmitted() {
        boolean submitted = true;
        for (Reference r: this)
            submitted = submitted && r.isSuccessfullySubmitted();
        return submitted;
    }

}
