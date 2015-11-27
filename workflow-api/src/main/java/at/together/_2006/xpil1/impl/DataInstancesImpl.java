/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.together._2006.xpil1.impl;

import at.together._2006.xpil1.DataInstance;
import at.together._2006.xpil1.DataInstances;
import at.together._2006.xpil1.InstanceExtendedAttributes;
import java.util.List;


/**
 *
 * @author slavb
 */
public class DataInstancesImpl extends DataInstances {

    public DataInstancesImpl() {
    }

    public DataInstancesImpl(List<DataInstance> stringDataInstancesAndStringArrayDataInstancesAndBooleanDataInstances, InstanceExtendedAttributes instanceExtendedAttributes) {
        super(stringDataInstancesAndStringArrayDataInstancesAndBooleanDataInstances, instanceExtendedAttributes);
    }


    /*@Override
     public List<DataInstance> getStringDataInstancesAndStringArrayDataInstancesAndBooleanDataInstances() {
     if (stringDataInstancesAndStringArrayDataInstancesAndBooleanDataInstances == null) {
     stringDataInstancesAndStringArrayDataInstancesAndBooleanDataInstances = SetUniqueList.decorate(new ArrayList());
     }
     return this.stringDataInstancesAndStringArrayDataInstancesAndBooleanDataInstances;
     }*/
    DataInstance getDataInstance(String id) {
        DataInstance result = null;
        if (stringDataInstancesAndStringArrayDataInstancesAndBooleanDataInstances != null) {
            for (DataInstance di : stringDataInstancesAndStringArrayDataInstancesAndBooleanDataInstances) {
                if (id.equals(di.getId())) {
                    result = di;
                    break;
                }
            }
        }
        return result;
    }

}
