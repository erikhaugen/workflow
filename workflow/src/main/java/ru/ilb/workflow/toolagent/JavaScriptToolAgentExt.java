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
 */package ru.ilb.workflow.toolagent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.enhydra.shark.admin.repositorymanagement.RepositoryManager;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.ApplicationBusy;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotDefined;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotStarted;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.toolagent.JavaScriptToolAgent;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.slf4j.LoggerFactory;

/**
 *
 * @author slavb
 */
public class JavaScriptToolAgentExt extends JavaScriptToolAgent {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JavaScriptToolAgentExt.class);

    private String applicationsPath;

    public static org.slf4j.Logger getLogger() {
        return logger;
    }

    Scriptable scope;

    @Override
    public void invokeApplication(WMSessionHandle shandle, long handle, WMEntity appInfo, WMEntity toolInfo, String applicationName, String procInstId, String assId, AppParameter[] parameters, Integer appMode) throws ApplicationNotStarted, ApplicationNotDefined, ApplicationBusy, ToolAgentGeneralException {
        try {
            String xpdlRepository = RepositoryManager.getInstance().getPathToXPDLRepositoryFolder();
            applicationsPath = xpdlRepository + "/" + toolInfo.getPkgId() + "/" + toolInfo.getWpId() + "/applications";
            script = getScript(toolInfo.getId() + ".js");
        } catch (Throwable ex) {
            throw new ToolAgentGeneralException(ex);
        }
        super.invokeApplication(shandle, handle, appInfo, toolInfo, applicationName, procInstId, assId, parameters, appMode);
    }

    @Override
    protected Scriptable prepareContext(Context cx, AppParameter[] context) throws Exception {
        scope = super.prepareContext(cx, context);
        scope.put("toolagent", scope, this);
        return scope;
    }

    public String getScript(String name) throws IOException {
        String path = applicationsPath + "/" + name;
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        String result = new String(encoded, StandardCharsets.UTF_8);
        return result;
    }

    public void include(String name) throws IOException {
        String source = getScript(name);
        try {
            Context context = Context.enter();
            context.evaluateString(scope, source, name, 1, null);
        } finally {
            Context.exit();
        }
    }

}
