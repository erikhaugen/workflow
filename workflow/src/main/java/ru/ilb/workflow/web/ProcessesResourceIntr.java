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
package ru.ilb.workflow.web;

import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.utilities.interfacewrapper.SharkInterfaceWrapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.ilb.workflow.search.ProcessFilterVisitor;

/**
 *
 * @author slavb
 */
@Component
class ProcessesResourceIntr {

    private SearchContext searchContext;

    public void setSearchContext(SearchContext searchContext) {
        this.searchContext = searchContext;
    }

    @Transactional
    void checkDeadlinesWithFiltering(String filter) {
        try {
            WMSessionHandle shandle = SharkInterfaceWrapper.getSessionHandle(AuthorizationHandler.getAuthorisedUser(), null);
            WMFilter f = getProcessFilter(shandle, filter);
            SharkInterfaceWrapper.getShark().getExecutionAdministration().checkDeadlinesWithFiltering(shandle, f);
            SharkInterfaceWrapper.getShark().getExecutionAdministration().reevaluateDeadlinesWithFiltering(shandle, f);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    WMFilter getProcessFilter(WMSessionHandle shandle, String filter) {
        WMFilter f = null;
        if (filter != null) {
            SearchCondition<SearchBean> sc = searchContext.getCondition(filter, SearchBean.class);
            ProcessFilterVisitor<SearchBean> visitor = new ProcessFilterVisitor<>(shandle);
            sc.accept(visitor);
            f = visitor.getQuery();
        }
        return f;
    }

    void onProcessEdit(WMSessionHandle shandle, String processId) throws Exception{
        try {
            SharkInterfaceWrapper.getShark().getExecutionAdministration().reevaluateDeadlinesForProcesses(shandle, new String[]{processId});
        } catch (Exception ex) {
            if (!ex.getMessage().contains("closed")) {
                throw ex;
            }
        }
    }

}
