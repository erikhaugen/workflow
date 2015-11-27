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
package ru.ilb.workflow.assignment;

import java.util.Iterator;
import java.util.List;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.assignment.PerformerData;
import org.enhydra.shark.assignment.historyrelated.HistoryRelatedAssignmentManager;
import org.enhydra.shark.utilities.interfacewrapper.SharkInterfaceWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import static ru.ilb.workflow.assignment.HistoryRelatedAssignmentManagerExt.orgunitIdVariable;
import ru.ilb.workflow.orgtree.OrgTreeManager;
import ru.ilb.workflow.utils.SpringApplicationContext;

/**
 *
 * @author slavb
 */
public class OrgTreeAssignmentManager extends HistoryRelatedAssignmentManager {

    protected static String orgunitIdVariable = "orgunitId";

    @Override
    public List getDefaultAssignments(WMSessionHandle shandle, String procId, String actId, String processRequesterId, PerformerData xpdlParticipant, List xpdlResponsibleParticipants) throws Exception {
        WAPI wapi = SharkInterfaceWrapper.getShark().getWAPIConnection();
        List<String> defaultAssignments = super.getDefaultAssignments(shandle, procId, actId, processRequesterId, xpdlParticipant, xpdlResponsibleParticipants);
        WMAttribute orgunitId = wapi.getProcessInstanceAttributeValue(shandle, procId, orgunitIdVariable);
        OrgTreeManager orgTreeManager = SpringApplicationContext.getApplicationContext().getBean(OrgTreeManager.class);
        if (orgunitId != null && orgunitId.getValue()!=null) {
            List<String> officeUsers=orgTreeManager.getAllOrgunitMembers((String)orgunitId.getValue());
            for (Iterator<String> iterator = defaultAssignments.iterator(); iterator.hasNext();) {
                String user = iterator.next();
                if (!getUserGroupManager().doesUserBelongToGroup(shandle, orgTreeManager.getAllOfficesGroup(), user) && !officeUsers.contains(user)) {
                    iterator.remove();
                }
            }
        }
        return defaultAssignments;
    }

}
