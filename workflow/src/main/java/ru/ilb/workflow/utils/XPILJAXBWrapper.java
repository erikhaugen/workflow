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
package ru.ilb.workflow.utils;

import at.together._2006.xpil1.ActivityInstance;
import at.together._2006.xpil1.DataInstance;
import at.together._2006.xpil1.DateTimeDataInstance;
import at.together._2006.xpil1.ExtendedWorkflowFacilityInstance;
import at.together._2006.xpil1.MainWorkflowProcessInstance;
import at.together._2006.xpil1.WorkflowProcessInstance;
import at.together._2006.xpil1.ManualActivityInstance;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.utilities.interfacewrapper.SharkInterfaceWrapper;

/**
 *
 * @author slavb
 */
public class XPILJAXBWrapper {

    public static void editProcess(WMSessionHandle shandle, WorkflowProcessInstance process, String processId) throws Exception {
        if (process.getDataInstances() != null) {
            ExtendedWorkflowFacilityInstance extendedworkflowfacilityinstance = new ExtendedWorkflowFacilityInstance();
            for (DataInstance di : process.getDataInstances().getStringDataInstancesAndStringArrayDataInstancesAndBooleanDataInstances()) {
                extendedworkflowfacilityinstance.getUsersAndUsersAndPackageInstances().add(di);
            }
            String xpil=XPILJAXBUtils.toString(extendedworkflowfacilityinstance);
            xpil=xpil.replace("+04:00\" Id=\"nextCallDate\"", "\" Id=\"nextCallDate\""); //FIXME
            SharkInterfaceWrapper.getShark().getXPILHandler().setSpecifiedProcessVariables(shandle, processId, xpil);
        }
        if (process.getName() != null || process.getInstanceDescription() != null || process.getInstancePriority() != null) {
            ExtendedWorkflowFacilityInstance extendedworkflowfacilityinstance = new ExtendedWorkflowFacilityInstance();
            MainWorkflowProcessInstance processcopy = new MainWorkflowProcessInstance();
            processcopy.setId(processId);
            processcopy.setName(process.getName());
            processcopy.setInstanceDescription(process.getInstanceDescription());
            processcopy.setInstancePriority(process.getInstancePriority());
            extendedworkflowfacilityinstance.getUsersAndUsersAndPackageInstances().add(processcopy);
            String xpil=XPILJAXBUtils.toString(extendedworkflowfacilityinstance);
            SharkInterfaceWrapper.getShark().getXPILHandler().setProcessInfo(shandle, xpil);
        }

    }
    public static void editActivity(WMSessionHandle shandle, ActivityInstance activity,String processId, String activityId) throws Exception {
        if(activity.getDataInstances()!=null){
            ExtendedWorkflowFacilityInstance extendedworkflowfacilityinstance = new ExtendedWorkflowFacilityInstance();
            for (DataInstance di : activity.getDataInstances().getStringDataInstancesAndStringArrayDataInstancesAndBooleanDataInstances()) {
                extendedworkflowfacilityinstance.getUsersAndUsersAndPackageInstances().add(di);
            }
            String xpil=XPILJAXBUtils.toString(extendedworkflowfacilityinstance);
            xpil=xpil.replace("+04:00\" Id=\"nextCallDate\"", "\" Id=\"nextCallDate\""); //FIXME
            SharkInterfaceWrapper.getShark().getXPILHandler().setSpecifiedActivityVariables(shandle, processId, activityId, xpil);
        }
        if (activity.getName() != null || activity.getInstanceDescription() != null || activity.getInstancePriority() != null) {
            ExtendedWorkflowFacilityInstance extendedworkflowfacilityinstance = new ExtendedWorkflowFacilityInstance();
            ManualActivityInstance activitycopy = new ManualActivityInstance();
            activitycopy.setId(activityId);
            activitycopy.setName(activity.getName());
            activitycopy.setInstanceDescription(activity.getInstanceDescription());
            activitycopy.setInstancePriority(activity.getInstancePriority());
            extendedworkflowfacilityinstance.getUsersAndUsersAndPackageInstances().add(activitycopy);
            String xpil=XPILJAXBUtils.toString(extendedworkflowfacilityinstance);
            SharkInterfaceWrapper.getShark().getXPILHandler().setActivityInfo(shandle, xpil);
        }


    }

}
