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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import org.enhydra.jxpdl.XMLUtil;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMActivityInstance;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.common.ActivityFilterBuilder;
import org.enhydra.shark.api.common.SharkConstants;
import org.enhydra.shark.api.internal.assignment.PerformerData;
import org.enhydra.shark.utilities.interfacewrapper.SharkInterfaceWrapper;
import org.enhydra.shark.utilities.wmentity.WMEntityUtilities;
import org.slf4j.LoggerFactory;
import ru.ilb.workflow.utils.TimeUtils;

public class HistoryRelatedAssignmentManagerExt extends OrgTreeAssignmentManager {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(HistoryRelatedAssignmentManagerExt.class);

    protected static String reassignBefore = "ReassignBefore";
    protected static String reassignIfInGroup = "ReassignIfInGroup";
    protected static String reassignIfPriorityGreater = "reassignIfPriorityGreater";

    @Override
    protected List getAssignmentsForActDefId(WMSessionHandle shandle,
            String procId,
            String actId,
            String processRequesterId,
            PerformerData xpdlParticipant,
            List xpdlResponsibleParticipants,
            String actDefId,
            boolean fallbackToDefault) throws Exception {
        WMEntity ent = Shark.getInstance().getAdminMisc().getActivityDefinitionInfo(shandle, procId, actId);
        String[][] actExtAttribs = WMEntityUtilities.getExtAttribNVPairs(shandle, Shark.getInstance().getXPDLBrowser(), ent);
        String reassignBeforeValue = XMLUtil.getExtendedAttributeValue(actExtAttribs, reassignBefore);
        String reassignGroupValue = XMLUtil.getExtendedAttributeValue(actExtAttribs, reassignIfInGroup);
        String reassignPriorityValueStr = XMLUtil.getExtendedAttributeValue(actExtAttribs, reassignIfPriorityGreater);
        Integer reassignPriorityValue = null;
        if (reassignPriorityValueStr != null && !reassignPriorityValueStr.isEmpty()) {
            try {
                reassignPriorityValue = Integer.parseInt(reassignPriorityValueStr);
            } catch (NumberFormatException ex) {
                throw new WebApplicationException("Ошибка преобразования, в число, параметра reassignIfPriorityGreater (" + reassignPriorityValueStr + ")", 550);
            }
        }

        List defaultAssignment = getDefaultAssignments(shandle, procId, actId, processRequesterId, xpdlParticipant, xpdlResponsibleParticipants);
        WAPI wapi = SharkInterfaceWrapper.getShark().getWAPIConnection();
        WMActivityInstance act = wapi.getActivityInstance(shandle, procId, actId);
        WMAttribute callActionAttr = wapi.getProcessInstanceAttributeValue(shandle, procId, "callAction");
        if (act != null && (reassignPriorityValue == null || reassignPriorityValue < act.getPriority())
                && reassignBeforeValue != null && !reassignBeforeValue.isEmpty()
                && reassignGroupValue != null && !reassignGroupValue.isEmpty()
                && (callActionAttr == null || !callActionAttr.getValue().equals("NOT_AVAILIABLE"))) {
            Date createdTime = new Date(Shark.getInstance().getAdminMisc().getActivityCreatedTime(shandle, procId, actId));
            Date xTime = TimeUtils.setTimeInDay(createdTime, reassignBeforeValue);
            Date curTime = new java.util.Date();
            String assignedPerformer = null;
            WMAttribute assignedPerformerAttr = wapi.getProcessInstanceAttributeValue(shandle, procId, "assignedPerformer");
            if (assignedPerformerAttr != null) {
                assignedPerformer = (String) assignedPerformerAttr.getValue();
            }
            if (assignedPerformer == null) {
                assignedPerformer = getPrevPerformerOfActDefId(shandle, procId, actDefId);
            }
            if (assignedPerformer == null) {
                String actDefIdOther = XMLUtil.getExtendedAttributeValue(actExtAttribs, extAttrAssignToPerformerOfActivity);
                if (actDefIdOther != null) {
                    assignedPerformer = getPrevPerformerOfActDefId(shandle, procId, actDefIdOther);
                }
            }
            if (assignedPerformer != null) {
                if (defaultAssignment.contains(assignedPerformer)) {
                    if ((curTime.before(xTime) || getUserGroupManager().doesUserBelongToGroup(shandle, reassignGroupValue, assignedPerformer))) {
                        List result = new ArrayList();
                        result.add(assignedPerformer);
                        logger.debug("actId={}, assigned to {}", actId, assignedPerformer);
                        return result;
                    } else {
                        logger.debug("actId={}, execution time after {} and performer " + (assignedPerformer == null ? "null" : assignedPerformer) + " does not belong to group" + reassignGroupValue, actId, xTime.toString());
                    }
                } else {
                    logger.debug("DefaultAssignments don't contain performer {}", assignedPerformer);
                }
            }
        }

        return defaultAssignment;
    }

    @Override
    protected String getPrevPerformerOfActDefId(WMSessionHandle shandle, String procId, String actDefId) throws Exception {
        String result = null;
        WAPI wapi = SharkInterfaceWrapper.getShark().getWAPIConnection();
        ActivityFilterBuilder fb = SharkInterfaceWrapper.getShark().getActivityFilterBuilder();
        WMFilter f = fb.addProcessIdEquals(shandle, procId);
        f = fb.and(shandle, f, fb.addStateEquals(shandle, SharkConstants.STATE_CLOSED_COMPLETED));
        f = fb.and(shandle, f, fb.addDefinitionIdEquals(shandle, actDefId));
        fb.setOrderByLastStateTime(shandle, f, false);
        fb.setLimit(shandle, f, 1);
        WMActivityInstance[] acts = wapi.listActivityInstances(shandle, f, true).getArray();
        if (acts.length > 0) {
            result = Shark.getInstance().getAdminMisc().getActivityResourceUsername(shandle, procId, acts[0].getId());
        }
        return result;
    }

}
