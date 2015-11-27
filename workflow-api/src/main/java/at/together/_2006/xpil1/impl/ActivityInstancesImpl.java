/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.together._2006.xpil1.impl;

import at.together._2006.xpil1.ActivityInstance;
import at.together._2006.xpil1.ActivityInstances;
import java.util.List;

/**
 *
 * @author slavb
 */
public class ActivityInstancesImpl extends ActivityInstances{

    public ActivityInstance getActivity(String id){
        ActivityInstance result = null;
        if (manualActivityInstancesAndToolActivityInstancesAndBlockActivityInstances != null) {
            for (ActivityInstance ai : manualActivityInstancesAndToolActivityInstancesAndBlockActivityInstances) {
                if (id.equals(ai.getId())) {
                    result = ai;
                    break;
                }
            }
        }
        return result;
    }
    public ActivityInstance getActivityByDefinitionId(String definitionId){
        ActivityInstance result = null;
        if (manualActivityInstancesAndToolActivityInstancesAndBlockActivityInstances != null) {
            for (ActivityInstance ai : manualActivityInstancesAndToolActivityInstancesAndBlockActivityInstances) {
                if (definitionId.equals(ai.getDefinitionId())) {
                    result = ai;
                    break;
                }
            }
        }
        return result;
    }
    public ActivityInstance getActivityByDefinitionId(List<String> definitionId){
        ActivityInstance result = null;
        if (manualActivityInstancesAndToolActivityInstancesAndBlockActivityInstances != null) {
            for (ActivityInstance ai : manualActivityInstancesAndToolActivityInstancesAndBlockActivityInstances) {
                if (definitionId.contains(ai.getDefinitionId())) {
                    result = ai;
                    break;
                }
            }
        }
        return result;
    }

}
