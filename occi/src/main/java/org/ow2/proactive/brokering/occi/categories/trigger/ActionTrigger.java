package org.ow2.proactive.brokering.occi.categories.trigger;

import java.util.List;
import java.util.ArrayList;
import org.ow2.proactive.brokering.occi.Attribute;
import org.ow2.proactive.brokering.occi.categories.BaseCategory;

public class ActionTrigger extends BaseCategory {

    public static final String OCCI_CORE_ID = "occi.core.id";
    public static final String OCCI_MONITORING_PERIODMS = "occi.monitoring.periodms";
    public static final String OCCI_CONDITION_SCRIPT = "occi.monitoring.condition";
    public static final String OCCI_MONITORING_INITACTION = "occi.monitoring.initaction";
    public static final String OCCI_MONITORING_TRUEACTION = "occi.monitoring.trueaction";
    public static final String OCCI_MONITORING_FALSEACTION = "occi.monitoring.falseaction";
    public static final String OCCI_MONITORING_STOPACTION = "occi.monitoring.stopaction";
    public static final String OCCI_MONITORING_ACTION = "occi.monitoring.action";
    public static final String OCCI_MONITORING_METADATA = "occi.monitoring.metadata";

    public ActionTrigger() { }

    @Override
    public List<Attribute> getAttributes() {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        boolean mutable = true;
        boolean required = true;
        attributeList.add(new Attribute(OCCI_MONITORING_PERIODMS, mutable, !required));

        attributeList.add(new Attribute(OCCI_CONDITION_SCRIPT, mutable, required));
        attributeList.add(new Attribute(OCCI_MONITORING_ACTION, mutable, !required));
        attributeList.add(new Attribute(OCCI_MONITORING_FALSEACTION, mutable, !required));
        attributeList.add(new Attribute(OCCI_MONITORING_TRUEACTION, mutable, !required));
        attributeList.add(new Attribute(OCCI_MONITORING_METADATA, mutable, !required));

        return attributeList;
    }

    @Override
    public String getScheme() {
        return "http://schemas.ogf.org/occi/#";
    }

}
