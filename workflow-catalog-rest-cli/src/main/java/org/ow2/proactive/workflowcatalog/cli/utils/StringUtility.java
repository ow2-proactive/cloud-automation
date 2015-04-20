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


package org.ow2.proactive.workflowcatalog.cli.utils;

import java.io.*;
import java.util.Map;

import groovy.json.JsonOutput;
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
                Object obj = object(entry.getValue().getSerializedValue());
                builder.append("'");
                builder.append(jsonPretty(obj.toString()));
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

    public static String jsonPretty(String json) {
        return JsonOutput.prettyPrint(json);
    }

}
