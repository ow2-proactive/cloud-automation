package org.ow2.proactive.workflowcatalog.cli.utils;

import java.io.*;
import java.util.Map;
import org.apache.commons.codec.binary.StringUtils;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskResultData;
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

    public static String string(JobResultData jobResult) {
        StringBuilder builder = new StringBuilder();
        builder.append("   ");
        builder.append(jobResult.getId().getId());
        builder.append(" (\n");
        builder.append(stringResult(jobResult.getAllResults()));
        builder.append("\n   )\n");
        return builder.toString();
    }

    public static String string(WorkflowBean workflow) {
        StringBuilder builder = new StringBuilder();
        builder.append("   ");
        builder.append(workflow.name);
        builder.append(" (\n");
        builder.append(string(workflow.variables));
        builder.append("\n");
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
        return ret.substring(0, (ret.isEmpty()?0:ret.length()-1));
    }

    public static String stringResult(Map<String, TaskResultData> map) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, TaskResultData> entry: map.entrySet()) {
            builder.append("      '");
            builder.append(entry.getKey());
            builder.append("'=");
            if (entry.getValue() == null) {
                builder.append("null");
            } else {
                builder.append("'");
                builder.append(object(entry.getValue().getSerializedValue()));
                builder.append("'");
            }
            builder.append("\n");
        }
        String ret = builder.toString();
        return ret.substring(0, (ret.isEmpty()?0:ret.length()-1));
    }

    public static Object object(byte[] bytes) {
        if (bytes == null) {
            return "[NULL]";
        }
        try {
            return new ObjectInputStream(new ByteArrayInputStream(bytes))
                    .readObject();
        } catch (ClassNotFoundException cnfe) {
            return String.format("[De-serialization error : %s]",
                                 cnfe.getMessage());
        } catch (IOException ioe) {
            return String.format("[De-serialization error : %s]",
                                 ioe.getMessage());
        }
    }

}
