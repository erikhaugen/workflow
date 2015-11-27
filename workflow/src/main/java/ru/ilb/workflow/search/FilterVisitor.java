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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.apache.cxf.jaxrs.ext.search.OrSearchCondition;
import org.apache.cxf.jaxrs.ext.search.PrimitiveStatement;
import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.ext.search.visitor.AbstractSearchConditionVisitor;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.common.FilterBuilder;

/**
 *
 * @author slavb
 */
public abstract class FilterVisitor<T> extends AbstractSearchConditionVisitor<T, WMFilter> {

    protected FilterBuilder fb;
    protected WMFilter filterEx;
    protected WMSessionHandle shandle;
    protected Stack<List<WMFilter>> predStack = new Stack<>();

    public FilterVisitor(WMSessionHandle shandle) {
        super(null);
        this.shandle = shandle;
    }

    @Override
    public void visit(SearchCondition<T> sc) {
        try {
            if (fb == null) {
                fb = getFilterBuilder();
                predStack.push(new ArrayList<WMFilter>());
            }
            if (filterEx == null) {
                filterEx = fb.createEmptyFilter(shandle);
            }

            PrimitiveStatement statement = sc.getStatement();
            if (statement != null) {
                WMFilter f = buildWMFilter(sc.getStatement());
                if (f != null) {
                    predStack.peek().add(f);
                }
            } else {
                predStack.push(new ArrayList<WMFilter>());
                // composite expression, ex "a > b;c < d"
                for (SearchCondition<T> condition : sc.getSearchConditions()) {
                    condition.accept(this);
                }
                List<WMFilter> predsList = predStack.pop();
                if (predsList.size() > 0) {
                    WMFilter[] preds = predsList.toArray(new WMFilter[predsList.size()]);
                    WMFilter newPred;
                    if (sc instanceof OrSearchCondition) {
                        newPred = fb.orForArray(shandle, preds);
                    } else {
                        newPred = fb.andForArray(shandle, preds);
                    }
                    predStack.peek().add(newPred);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
    protected abstract FilterBuilder getFilterBuilder()  throws Exception;

    protected abstract WMFilter buildWMFilter(PrimitiveStatement statement) throws Exception;

    @Override
    public WMFilter getQuery() {
        if (!this.predStack.isEmpty() && !this.predStack.get(0).isEmpty()) {
            WMFilter filter = this.predStack.get(0).get(0);
            filter.setLimit(filterEx.getLimit());
            filter.setStartPosition(filterEx.getStartPosition());
            filter.setOrderBy(filterEx.getOrderBy());
            return filter;
        } else { // не было никаких условий кроме limit,order by,..
            return filterEx;
        }
    }
}
