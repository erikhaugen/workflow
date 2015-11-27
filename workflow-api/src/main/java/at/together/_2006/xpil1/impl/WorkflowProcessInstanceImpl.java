/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.together._2006.xpil1.impl;

import at.together._2006.xpil1.ActivityInstance;
import at.together._2006.xpil1.EventAudits;
import at.together._2006.xpil1.InstanceExtendedAttributes;
import at.together._2006.xpil1.WorkflowProcessInstance;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.wfmc._2002.xpdl1.WorkflowProcess;

/**
 *
 * @author slavb
 */
public class WorkflowProcessInstanceImpl extends WorkflowProcessInstance{

    public WorkflowProcessInstanceImpl() {
    }

    public WorkflowProcessInstanceImpl(String instanceDescription, Date instanceLimit, Integer instancePriority, DataInstancesImpl dataInstances, EventAudits eventAudits, InstanceExtendedAttributes instanceExtendedAttributes, String id, String definitionId, String name, String state, Date created, Date started, Date finished, Map<QName, String> otherAttributes, ActivityInstancesImpl activityInstances, WorkflowProcess workflowProcess, String requesterUsername, String factoryId) {
        super(instanceDescription, instanceLimit, instancePriority, dataInstances, eventAudits, instanceExtendedAttributes, id, definitionId, name, state, created, started, finished, otherAttributes, activityInstances, workflowProcess, requesterUsername, factoryId);
    }

    public ActivityInstance getActivity(String id){
        return activityInstances!=null?activityInstances.getActivity(id):null;
    }
    public ActivityInstance getActivityByDefinitionId(String definitionId){
        return activityInstances!=null?activityInstances.getActivityByDefinitionId(definitionId):null;
    }

    public ActivityInstance getActivityByDefinitionId(List<String> definitionId){
        return activityInstances!=null?activityInstances.getActivityByDefinitionId(definitionId):null;
    }

}
