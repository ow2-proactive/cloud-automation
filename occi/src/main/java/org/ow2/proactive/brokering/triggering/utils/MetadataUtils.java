package org.ow2.proactive.brokering.triggering.utils;

import org.ow2.proactive.brokering.occi.categories.trigger.ActionTrigger;
import java.util.Map;

public class MetadataUtils {

    public static String extractMetadata(Map<String, String> args) {
        return args.get(ActionTrigger.OCCI_MONITORING_METADATA);
    }

    public static void writeMetadataToServer(Map<String, String> args, String metadata) {
        args.put(ActionTrigger.OCCI_MONITORING_METADATA, metadata);
    }
}
