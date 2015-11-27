/**
 * Copyright (C) 2015 Bystrobank, JSC
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see http://www.gnu.org/licenses
 */
package ru.ilb.workflow.toolagent;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.utilities.logging.LoggingUtilities;

/**
 *
 * @author slavb
 */
public class WebClient /*extends AbstractToolAgent*/ {

    public static void execute(AppParameter method, AppParameter url, AppParameter headers, AppParameter data, AppParameter response_code, AppParameter response) throws ToolAgentGeneralException {
        response.the_value = execute(method.the_value.toString(), url.the_value.toString(), (String[]) headers.the_value, data.the_value);
    }
    public static String execute(String method, String url, String[] headers, Object data) throws ToolAgentGeneralException {
        int http_code;
        String response;
        try {
            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod(method);
            if (headers != null) {
                for (String h : headers) {
                    String[] keyval = h.split(":");
                    if (keyval.length == 2) {
                        connection.setRequestProperty(keyval[0].trim(), keyval[1].trim());
                    }
                }
            }
            //connection.setDoInput(true);
            if (data != null) {
                String dataStr;
                if (data instanceof Map) {
                    StringBuilder result = new StringBuilder();
                    for (Map.Entry<String, String> entry : ((Map<String, String>) data).entrySet()) {
                        if (result.length() > 0) {
                            result.append("&");
                        }
                        result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                        result.append("=");
                        result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                    }
                    dataStr = result.toString();
                } else {
                    dataStr = data.toString();
                }
                connection.setRequestProperty("Content-Length", ""
                        + Integer.toString(dataStr.getBytes().length));
                connection.setDoOutput(true);
                try (OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8")) {
                    out.write(dataStr);
                    out.flush();
                }
            } else {
                connection.connect();
            }
            http_code = connection.getResponseCode();
            response = new java.util.Scanner(http_code < 400 ? connection.getInputStream() : connection.getErrorStream(), "UTF-8").useDelimiter("\\A").next();
        } catch (Exception ex) {
            LoggingUtilities.log4jInfo(null, "WebClient failed", ex, true, true);
            throw new ToolAgentGeneralException(ex);
        }
        if (http_code >= 400) {
            LoggingUtilities.log4jInfo(null, "WebClient failed http_code=" + http_code + " response=" + response, null, true, true);
            throw new ToolAgentGeneralException(response);
        }
        return response;
    }

}
