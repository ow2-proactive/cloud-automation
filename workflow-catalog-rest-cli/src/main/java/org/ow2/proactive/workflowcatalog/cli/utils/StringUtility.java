package org.ow2.proactive.workflowcatalog.cli.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.codec.binary.StringUtils;
import org.ow2.proactive.workflowcatalog.JobResult;
import org.ow2.proactive.workflowcatalog.api.utils.formatter.beans.WorkflowBean;

public class StringUtility {
    public static boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    public static boolean isEmpty(String[] array) {
        return array == null || array.length == 0;
    }


    public static String objectArrayFormatterAsString(ObjectArrayFormatter oaf) {
        return Tools.getStringAsArray(oaf);
    }

    public static String responseAsString(HttpResponseWrapper response) {
            return StringUtils.newStringUtf8(response.getContent());
    }

    public static String stackTraceAsString(Throwable error) {
        StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out);
        error.printStackTrace(writer);
        writer.flush();
        return out.toString();
    }

    public static String string(JobResult jobResult) {
        StringBuilder builder = new StringBuilder();
        builder.append("   ");
        builder.append(jobResult.jobId);
        builder.append(" (\n");
        builder.append(string(jobResult.taskResults));
        builder.append("\n   )\n");
        return builder.toString();
    }

    public static String string(WorkflowBean workflow) {
        StringBuilder builder = new StringBuilder();
        builder.append("   ");
        builder.append(workflow.name);
        builder.append(" (\n");
        builder.append(string(workflow.variables));
        builder.append(string(workflow.genericInformation));
        builder.append("\n   )\n");
        return builder.toString();
    }

    public static String string(Map<String, String> map) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry: map.entrySet()) {
            builder.append("      '");
            builder.append(entry.getKey());
            builder.append("'=");
            if (entry.getValue() == null) {
                builder.append("null");
            } else {
                builder.append("'");
                builder.append(entry.getValue());
                builder.append("'");
            }
            builder.append("\n");
        }
        String ret = builder.toString();
        return ret.substring(0, ret.length() - 1);
    }

}
