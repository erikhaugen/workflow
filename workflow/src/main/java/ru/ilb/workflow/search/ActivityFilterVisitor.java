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
package ru.ilb.workflow.search;

import javax.xml.bind.DatatypeConverter;
import org.apache.cxf.jaxrs.ext.search.ConditionType;
import org.apache.cxf.jaxrs.ext.search.PrimitiveStatement;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.common.ActivityFilterBuilder;
import org.enhydra.shark.api.common.FilterBuilder;
import org.enhydra.shark.api.common.ProcessFilterBuilder;
import org.enhydra.shark.utilities.interfacewrapper.SharkInterfaceWrapper;

/**
 *
 * @author slavb
 */
public class ActivityFilterVisitor<T> extends FilterVisitor<T> {

    private final String username;

    public ActivityFilterVisitor(WMSessionHandle shandle, String username) {
        super(shandle);
        this.username = username;
    }

    @Override
    protected WMFilter buildWMFilter(PrimitiveStatement statement) throws Exception {
        WMFilter filter = null;
        String varName = statement.getProperty();
        String varValue = (String) statement.getValue();
        ActivityFilterBuilder afb = (ActivityFilterBuilder) fb;
        switch (varName) {
            case "packageId":
                filter = afb.addPackageIdEquals(shandle, varValue);
                break;
            case "processDefId":
                filter = afb.addProcessDefIdEquals(shandle, varValue);
                break;
            case "processId":
                filter = afb.addProcessIdEquals(shandle, varValue);
                break;
            case "id":
                filter = afb.addIdEquals(shandle, varValue);
                break;
            case "assignment":
                if (!"*".equals(varValue)) {
                    int valueInt;
                    switch (varValue) {
                        case "ACCEPTED_AND_NON_ACCEPTED":
                            valueInt = ActivityFilterBuilder.ACCEPTED_AND_NON_ACCEPTED;
                            break;
                        case "ONLY_NON_ACCEPTED":
                            valueInt = ActivityFilterBuilder.ONLY_NON_ACCEPTED;
                            break;
                        case "ONLY_ACCEPTED":
                            valueInt = ActivityFilterBuilder.ONLY_ACCEPTED;
                        default:
                            throw new RuntimeException("hasAssignment values: *, ACCEPTED_AND_NON_ACCEPTED, ONLY_NON_ACCEPTED,ONLY_ACCEPTED");
                    }
                    filter = afb.addHasAssignmentForUser(shandle, username, valueInt);
                }
                break;
            case "definitionId":
                filter = afb.addDefinitionIdEquals(shandle, varValue);
                break;
            case "state":
                if (!"*".equals(varValue)) {
                    if (varValue.endsWith("*")) {
                        filter = afb.addStateStartsWith(shandle, varValue.substring(0, varValue.length() - 1));
                    } else {
                        filter = afb.addStateEquals(shandle, varValue);
                    }
                }
                break;
            case "lastStateTime":
                long varValueLong = DatatypeConverter.parseDateTime(varValue).getTime().getTime();
                switch (statement.getCondition()) {
                    case EQUALS:
                        filter = afb.addLastStateTimeEquals(shandle, varValueLong);
                        break;
                    case GREATER_THAN:
                        filter = afb.addLastStateTimeAfter(shandle, varValueLong);
                        break;
                    case LESS_THAN:
                        filter = afb.addLastStateTimeAfter(shandle, varValueLong);
                        break;
                    default:
                        throw new RuntimeException("Unsuported operator for field " + varName + ": " + statement.getCondition());
                }
                break;

            case "startPosition":
                afb.setStartPosition(shandle, filterEx, Integer.parseInt(varValue));
                break;
            case "limit":
                afb.setLimit(shandle, filterEx, Integer.parseInt(varValue));
                break;
            case "order":
                String[] orders = varValue.trim().split(":");
                for (String order : orders) {
                    String[] parts = order.trim().split(" ");
                    boolean asc = parts.length == 1 || !parts[1].equalsIgnoreCase("desc");
                    switch (parts[0]) {
                        case "id":
                            afb.setOrderById(shandle, filterEx, asc);
                            break;
                        case "activatedTime":
                            afb.setOrderByActivatedTime(shandle, filterEx, asc);
                            break;
                        case "priority":
                            afb.setOrderByPriority(shandle, filterEx, asc);
                            break;
                        case "lastStateTime":
                            afb.setOrderByLastStateTime(shandle, filterEx, asc);
                            break;
                        default:
                            throw new RuntimeException("Unknown order field " + parts[0]);
                    }
                }
                break;

            default:
                if (varName.startsWith("DataInstances.")) {
                    varName = statement.getProperty().substring(14);

                    switch (statement.getCondition()) {
                        case EQUALS:
                        case NOT_EQUALS:
                            filter = afb.addProcessVariableStringEquals(shandle, varName, varValue);
                            break;
                    }
                } else if (varName.startsWith("LongDataInstances.")) {
                    varName = statement.getProperty().substring(18);
                    switch (statement.getCondition()) {
                        case EQUALS:
                        case NOT_EQUALS:
                            filter = afb.addProcessVariableLongEquals(shandle, varName, Long.parseLong(varValue));
                            break;
                    }

                } else {
                    throw new RuntimeException("Unknown field " + varName + "=" + varValue);
                }
        }
        if (ConditionType.NOT_EQUALS.equals(statement.getCondition())) {
            filter = afb.not(shandle, filter);
        }
        return filter;
    }

    @Override
    protected FilterBuilder getFilterBuilder() throws Exception {
        return SharkInterfaceWrapper.getShark().getActivityFilterBuilder();
    }

}
