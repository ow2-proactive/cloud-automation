package org.ow2.proactive.workflowcatalog.api.utils.formatter.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ow2.proactive.workflowcatalog.Reference;
import org.ow2.proactive.workflowcatalog.References;
import com.google.gson.Gson;

public class ReferencesBean {

    public List<ReferenceBean> references;

    public ReferencesBean() {
        references = Collections.EMPTY_LIST;
    }

    public ReferencesBean(References referencesSrc) {
        references = new ArrayList<ReferenceBean>();
        for (Reference r: referencesSrc) {
            references.add(new ReferenceBean(r));
        }
    }

    public References generateReferences() {
        References r = new References();
        for (ReferenceBean ref : references) {
            r.add(ref.generateReference());
        }
        return r;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

}
