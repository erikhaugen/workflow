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
import org.enhydra.shark.api.common.FilterBuilder;
import org.enhydra.shark.api.common.ProcessFilterBuilder;
import org.enhydra.shark.utilities.interfacewrapper.SharkInterfaceWrapper;

/**
 *
 * @author slavb
 */
public class ProcessFilterVisitor<T> extends FilterVisitor<T> {

    public ProcessFilterVisitor(WMSessionHandle shandle) {
        super(shandle);
    }

    @Override
    protected WMFilter buildWMFilter(PrimitiveStatement statement) throws Exception {
        WMFilter filter = null;
        String varName = statement.getProperty();
        String varValue = (String) statement.getValue();
        ProcessFilterBuilder pfb = (ProcessFilterBuilder) fb;
        switch (varName) {
            case "id":
                filter = pfb.addIdEquals(shandle, varValue);
                break;
            case "packageId":
                filter = pfb.addPackageIdEquals(shandle, varValue);
                break;
            case "processDefId":
                filter = pfb.addProcessDefIdEquals(shandle, varValue);
                break;
            case "requesterId":
                filter = pfb.addRequesterIdEquals(shandle, varValue);
                break;
            case "createdTime":
                long varValueLong = DatatypeConverter.parseDateTime(varValue).getTime().getTime();
                switch (statement.getCondition()) {
                    case EQUALS:
                    case NOT_EQUALS:
                        filter = pfb.addCreatedTimeEquals(shandle, varValueLong);
                        break;
                    case GREATER_THAN:
                        filter = pfb.addCreatedTimeAfter(shandle, varValueLong);
                        break;
                    case LESS_THAN:
                        filter = pfb.addCreatedTimeBefore(shandle, varValueLong);
                        break;
                    default:
                        throw new RuntimeException("Unsuported operator for field " + varName + ": " + statement.getCondition());
                }
                break;
            case "activeActivitiesCount":
                long actCnt=Long.valueOf(varValue);
                switch (statement.getCondition()) {
                    case EQUALS:
                    case NOT_EQUALS:
                        filter = pfb.addActiveActivitiesCountEquals(shandle, actCnt);
                        break;
                    case GREATER_THAN:
                        filter = pfb.addActiveActivitiesCountGreaterThan(shandle, actCnt);
                        break;
                    case LESS_THAN:
                        filter = pfb.addActiveActivitiesCountLessThan(shandle, actCnt);
                        break;
                    default:
                        throw new RuntimeException("Unsuported operator for field " + varName + ": " + statement.getCondition());
                }
            case "state":
                if (!"*".equals(varValue)) {
                    if (varValue.endsWith("*")) {
                        filter = pfb.addStateStartsWith(shandle, varValue.substring(0, varValue.length() - 1));
                    } else {
                        filter = pfb.addStateEquals(shandle, varValue);
                    }
                }
                break;
            case "limit":
                pfb.setLimit(shandle, filterEx, Integer.parseInt(varValue));
                break;
            case "order":
                String[] order = varValue.trim().split(" ");
                boolean asc = order.length == 1 || !order[1].equals("desc");
                switch (order[0]) {
                    case "id":
                        pfb.setOrderById(shandle, filterEx, asc);
                        break;
                    case "priority":
                        pfb.setOrderByPriority(shandle, filterEx, asc);
                        break;
                    default:
                        throw new RuntimeException("Unknown order field " + order[0]);
                }
                break;
            default:
                if (varName.startsWith("DataInstances.")) {
                    varName = statement.getProperty().substring(14);

                    switch (statement.getCondition()) {
                        case EQUALS:
                        case NOT_EQUALS:
                            filter = pfb.addVariableStringEquals(shandle, varName, varValue);
                            break;
                    }
                } else if (varName.startsWith("LongDataInstances.")) {
                    varName = statement.getProperty().substring(18);
                    switch (statement.getCondition()) {
                        case EQUALS:
                        case NOT_EQUALS:
                            filter = pfb.addVariableLongEquals(shandle, varName, Long.parseLong(varValue));
                            break;
                    }

                } else {
                    throw new RuntimeException("Unknown field " + varName + "=" + varValue);
                }
        }
        if (ConditionType.NOT_EQUALS.equals(statement.getCondition())) {
            filter = pfb.not(shandle, filter);
        }
        return filter;
    }

    @Override
    protected FilterBuilder getFilterBuilder() throws Exception {
        return SharkInterfaceWrapper.getShark().getProcessFilterBuilder();
    }
}
