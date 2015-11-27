/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.together._2006.xpil1.impl;

import at.together._2006.xpil1.BooleanDataInstance;
import at.together._2006.xpil1.DataInstance;
import at.together._2006.xpil1.DateDataInstance;
import at.together._2006.xpil1.DateTimeDataInstance;
import at.together._2006.xpil1.EventAudits;
import at.together._2006.xpil1.ExecutionInstance;
import at.together._2006.xpil1.InstanceExtendedAttributes;
import at.together._2006.xpil1.LongDataInstance;
import at.together._2006.xpil1.StringDataInstance;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 *
 * @author slavb
 */
public class ExecutionInstanceImpl extends ExecutionInstance{

    public ExecutionInstanceImpl() {
    }

    public ExecutionInstanceImpl(String instanceDescription, Date instanceLimit, Integer instancePriority, DataInstancesImpl dataInstances, EventAudits eventAudits, InstanceExtendedAttributes instanceExtendedAttributes, String id, String definitionId, String name, String state, Date created, Date started, Date finished, Map<QName, String> otherAttributes) {
        super(instanceDescription, instanceLimit, instancePriority, dataInstances, eventAudits, instanceExtendedAttributes, id, definitionId, name, state, created, started, finished, otherAttributes);
    }

    public DataInstance getDataInstance(String id) {
        return dataInstances!=null?dataInstances.getDataInstance(id):null;
    }
    public String getStringValue(String id){
        return getDataInstance(id).unwrap(StringDataInstance.class).getValue();
    }
    public Boolean getBooleanValue(String id){
        return getDataInstance(id).unwrap(BooleanDataInstance.class).getValue();
    }
    public Date getDateValue(String id){
        return getDataInstance(id).unwrap(DateDataInstance.class).getValue();
    }
    public Date getDateTimeValue(String id){
        return getDataInstance(id).unwrap(DateTimeDataInstance.class).getValue();
    }
    public Long getLongValue(String id){
        return getDataInstance(id).unwrap(LongDataInstance.class).getValue();
    }
    public Integer getIntegerValue(String id){
        Integer result=null;
        Long value=getLongValue(id);
        if(value!=null){
            result=value.intValue();
        }
        return result;
    }
    public List<String> getStringArrayValue(String id){
        return getDataInstance(id).unwrap(StringArrayDataInstanceImpl.class).getValue();
    }

}
