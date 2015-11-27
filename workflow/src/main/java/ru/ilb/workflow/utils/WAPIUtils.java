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

import at.together._2006.xpil1.StringDataInstance;
import java.util.Iterator;
import javax.ws.rs.core.MultivaluedMap;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMActivityInstance;
import org.enhydra.shark.api.client.wfmc.wapi.WMActivityInstanceState;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItem;
import org.enhydra.shark.api.common.ActivityFilterBuilder;
import org.enhydra.shark.api.common.AssignmentFilterBuilder;
import org.enhydra.shark.api.common.SharkConstants;
import org.enhydra.shark.utilities.interfacewrapper.SharkInterfaceWrapper;
import org.enhydra.shark.utilities.namevalue.NameValueUtilities;
import ru.ilb.common.rs.DateConverter;

/**
 *
 * @author slavb
 */
public class WAPIUtils {

    public static Object StringToWMAttributeVal(WMAttribute attr, String vVal) {
        Object varVal = null;
        if (vVal.equals("null")) {
        } else if (attr.getType() == WMAttribute.BOOLEAN_TYPE) {
            if (vVal.toLowerCase().equals("true") || vVal.toLowerCase().equals("false")) {
                varVal = Boolean.valueOf(vVal.toLowerCase());
            } else {
                throw new RuntimeException("You've entered wrong value for variable type Boolean");
            }
        } else if (attr.getType() == WMAttribute.FLOAT_TYPE) {
            try {
                varVal = Double.valueOf(vVal);
            } catch (NumberFormatException ex) {
                throw new RuntimeException("You've entered wrong value for variable type Double");
            }
        } else if (attr.getType() == WMAttribute.INTEGER_TYPE) {
            try {
                varVal = Long.valueOf(vVal);
            } catch (NumberFormatException ex) {
                throw new RuntimeException("You've entered wrong value for variable type Long");
            }
        } else if (attr.getType() == WMAttribute.DATE_TYPE) {
            varVal = DateConverter.parseDate(vVal);
        } else if (attr.getType() == WMAttribute.DATETIME_TYPE) {
            varVal = DateConverter.parseDateTime(vVal);
        } else if (attr.getType() == WMAttribute.STRING_TYPE) {
            varVal = vVal;
        } else {
            throw new RuntimeException("This application can't handle variable which type is " + attr.getType());
        }
        return varVal;
    }

    public static WMActivityInstance findNextActivity(WMSessionHandle shandle, String userId, String procId) throws Exception {

        WAPI wapi = SharkInterfaceWrapper.getShark().getWAPIConnection();
        ActivityFilterBuilder fb = SharkInterfaceWrapper.getShark().getActivityFilterBuilder();
        WMFilter f = fb.addProcessIdEquals(shandle, procId);
        f = fb.and(shandle, f, fb.addStateStartsWith(shandle, SharkConstants.STATE_OPEN_NOT_RUNNING_NOT_STARTED));
        WMActivityInstance[] acts = wapi.listActivityInstances(shandle, f, true).getArray();
        WMWorkItem[] ass = null;
        if (NameValueUtilities.convertNameValueArrayToProperties(SharkInterfaceWrapper.getShark().getProperties())
                .getProperty("SharkKernel.createAssignments", "true")
                .equals("true")) {
            AssignmentFilterBuilder afb = SharkInterfaceWrapper.getShark().getAssignmentFilterBuilder();
            f = afb.addUsernameEquals(shandle, userId);
            //f = afb.and(shandle, f, afb.addProcessIdEquals(shandle, procId));
            ass = wapi.listWorkItems(shandle, f, true).getArray();
        }

        WMActivityInstance nextAct = null;
        for (WMActivityInstance act : acts) {
            if(act.getState().isClosed()) continue; //TEMP FIX
            if (ass == null) {
                nextAct = act;
                break;
            }
            for (WMWorkItem as : ass) {
                if (as.getActivityInstanceId().equals(act.getId())) {
                    nextAct = act;
                    break;
                }
            }
            if (nextAct != null) {
                break;
            }
        }
        return nextAct;
    }

    public static Boolean changeActivityState(WMSessionHandle shandle, WMActivityInstance act, String state) throws Exception {
        Boolean changed = false;
        if (state != null) {
            WAPI wapi = SharkInterfaceWrapper.getShark().getWAPIConnection();
            String procId = act.getProcessInstanceId();
            String actId = act.getId();
            String oldState = act.getState().stringValue();

            if (!oldState.equals(state)) {
                if (oldState.equals(SharkConstants.STATE_OPEN_NOT_RUNNING_NOT_STARTED) && state.equals(SharkConstants.STATE_CLOSED_COMPLETED)) {
                    wapi.changeActivityInstanceState(shandle, procId, actId, WMActivityInstanceState.valueOf(SharkConstants.STATE_OPEN_RUNNING));
                }
                wapi.changeActivityInstanceState(shandle, procId, actId, WMActivityInstanceState.valueOf(state));
                act.setState(WMActivityInstanceState.valueOf(state));
                changed=true;
            }
        }
        return changed;
    }

}
