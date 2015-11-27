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

import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.ext.search.fiql.FiqlParser;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMProcessInstance;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.ApplicationBusy;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotDefined;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotStarted;
import static org.enhydra.shark.api.internal.toolagent.ToolAgent.APP_STATUS_FINISHED;
import static org.enhydra.shark.api.internal.toolagent.ToolAgent.APP_STATUS_INVALID;
import static org.enhydra.shark.api.internal.toolagent.ToolAgent.APP_STATUS_RUNNING;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.toolagent.AbstractToolAgent;
import org.enhydra.shark.utilities.appparameter.AppParameterUtilities;
import org.enhydra.shark.utilities.interfacewrapper.SharkInterfaceWrapper;
import ru.ilb.workflow.search.ProcessFilterVisitor;

/**
 *
 * @author slavb
 */
public class ProcessToolAgent extends AbstractToolAgent {

    private static WMFilter getProcessFilter(WMSessionHandle shandle, String filter) {
        SearchCondition<SearchBean> sc = new FiqlParser<>(SearchBean.class).parse(filter);
        ProcessFilterVisitor<SearchBean> visitor = new ProcessFilterVisitor<>(shandle);
        sc.accept(visitor);
        WMFilter processfilter = visitor.getQuery();
        return processfilter;
    }

    public static WMProcessInstance[] getProcessList(WMSessionHandle shandle, String filter) throws Exception {
        WAPI wapi = SharkInterfaceWrapper.getShark().getWAPIConnection();
        WMFilter processfilter = getProcessFilter(shandle, filter);
        WMProcessInstance[] procsInst = wapi.listProcessInstances(shandle, processfilter, false).getArray();
        return procsInst;
    }

    @Override
    public void invokeApplication(WMSessionHandle shandle, long handle, WMEntity appInfo, WMEntity toolInfo, String applicationName, String procInstId, String assId, AppParameter[] parameters, Integer appMode) throws ApplicationNotStarted, ApplicationNotDefined, ApplicationBusy, ToolAgentGeneralException {
        super.invokeApplication(shandle, handle, appInfo, toolInfo, applicationName, procInstId, assId, parameters, appMode);
        try {
            status = APP_STATUS_RUNNING;
            String filter = (String) AppParameterUtilities.getParameterByName(this.parameters, "filter").the_value;
            WMProcessInstance[] procsInst = getProcessList(shandle, filter);

            status = APP_STATUS_FINISHED;
        } catch (Throwable ex) {
            status = APP_STATUS_INVALID;
            cus.error(shandle, "ActivityToolAgent - application "
                    + appName + " terminated incorrectly for [process="
                    + procInstId + ",assignment=" + assId + "]: " + ex);
            throw new ToolAgentGeneralException(ex);
        }
    }

}
