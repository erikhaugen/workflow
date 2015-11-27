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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.ext.search.fiql.FiqlParser;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMActivityInstance;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.common.ActivityFilterBuilder;
import org.enhydra.shark.api.common.SharkConstants;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.ApplicationBusy;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotDefined;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotStarted;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.toolagent.AbstractToolAgent;
import org.enhydra.shark.utilities.appparameter.AppParameterUtilities;
import org.enhydra.shark.utilities.interfacewrapper.SharkInterfaceWrapper;
import ru.ilb.workflow.search.ActivityFilterVisitor;
import ru.ilb.workflow.utils.WAPIUtils;

/**
 *
 * @author slavb
 */
public class ActivityToolAgent extends AbstractToolAgent {

    public static WMFilter getActivityFilter(WMSessionHandle shandle, String filter,String username) throws Exception {
        WMFilter wmfilter;
        FiqlParser fiqlParser = new FiqlParser<>(SearchBean.class);
        SearchCondition<SearchBean> sc = fiqlParser.parse(filter);
        ActivityFilterVisitor<SearchBean> visitor = new ActivityFilterVisitor<>(shandle, username);
        sc.accept(visitor);
        wmfilter = visitor.getQuery();
        return wmfilter;
    }
    public static WMActivityInstance[] getActivityList(WMSessionHandle shandle, String filter,String username) throws Exception {
        WAPI wapi = SharkInterfaceWrapper.getShark().getWAPIConnection();
        WMFilter wmfilter = getActivityFilter(shandle, filter, username);
        WMActivityInstance[] acts = wapi.listActivityInstances(shandle, wmfilter, false).getArray();
        List<WMActivityInstance> actslist = new ArrayList<>();
        //actslist.addAll(Arrays.asList(acts));
        for (WMActivityInstance act : acts) {
            if (!filter.contains("state==open") || act.getState().isOpen()) { //странный глюк при отборе open активити отбираются закрытые в рамках текущей транзакции, при этом act.getState().isOpen()=false
                actslist.add(act);
            }
        }
        return actslist.toArray(new WMActivityInstance[actslist.size()]);
    }

    public static WMFilter getActivityFilter(WMSessionHandle shandle, String processId, String[] definitionIdList, String state) throws Exception {
        ActivityFilterBuilder afb = SharkInterfaceWrapper.getShark().getActivityFilterBuilder();
        WMFilter filter = afb.addProcessIdEquals(shandle, processId);
        //String state = statePar.the_value.toString();
        if (!"*".equals(state)) {
            if (state.endsWith("*")) {
                filter = afb.and(shandle, filter, afb.addStateStartsWith(shandle, state.substring(0, state.length() - 1)));
            } else {
                filter = afb.and(shandle, filter, afb.addStateEquals(shandle, state));
            }
        }

        WMFilter definitionIdFilter[] = new WMFilter[definitionIdList.length];
        for (int i = 0; i < definitionIdList.length; i++) {
            definitionIdFilter[i] = afb.addDefinitionIdEquals(shandle, definitionIdList[i]);
        }
        filter = afb.and(shandle, filter, afb.orForArray(shandle, definitionIdFilter));
        return filter;

    }

    public static WMActivityInstance[] getActivityList(WMSessionHandle shandle, String processId, String[] definitionIdList, String state) throws Exception {
        WAPI wapi = SharkInterfaceWrapper.getShark().getWAPIConnection();
        WMFilter filter = getActivityFilter(shandle, processId, definitionIdList, state);
        WMActivityInstance[] acts = wapi.listActivityInstances(shandle, filter, false).getArray();
        List<WMActivityInstance> actslist = new ArrayList<>();
        for (WMActivityInstance act : acts) {
            if (!state.startsWith(SharkConstants.STATEPREFIX_OPEN) || act.getState().isOpen()) { //странный глюк при отборе open активити отбираются закрытые в рамках текущей транзакции, при этом act.getState().isOpen()=false
                actslist.add(act);
            }
        }
        return actslist.toArray(new WMActivityInstance[actslist.size()]);
    }

    @Override
    public void invokeApplication(WMSessionHandle shandle, long handle, WMEntity appInfo, WMEntity toolInfo, String applicationName, String procInstId, String assId, AppParameter[] parameters, Integer appMode) throws ApplicationNotStarted, ApplicationNotDefined, ApplicationBusy, ToolAgentGeneralException {
        super.invokeApplication(shandle, handle, appInfo, toolInfo, applicationName, procInstId, assId, parameters, appMode); //To change body of generated methods, choose Tools | Templates.
        try {
            status = APP_STATUS_RUNNING;
            String setState = (String) AppParameterUtilities.getParameterByName(this.parameters, "setState").the_value;
            AppParameter filter=AppParameterUtilities.getParameterByName(this.parameters, "filter");
            WMActivityInstance[] acts;
            if(filter==null) {
                String state = (String) AppParameterUtilities.getParameterByName(this.parameters, "state").the_value;
                String[] definitionIdList = (String[]) AppParameterUtilities.getParameterByName(this.parameters, "definitionId").the_value;
                acts = getActivityList(shandle, procInstId, definitionIdList, state);
            }else {
                acts = getActivityList(shandle,(String)filter.the_value, null);
            }

            for (WMActivityInstance act : acts) {
                WAPIUtils.changeActivityState(shandle, act, setState);
            }

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
