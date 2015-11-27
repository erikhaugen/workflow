/**
* Together Workflow Server
* Copyright (C) 2011 Together Teamsolutions Co., Ltd. 
* 
* This program is free software: you can redistribute it and/or modify 
* it under the terms of the GNU General Public License as published by 
* the Free Software Foundation, either version 3 of the License, or 
* (at your option) any later version. 
*
* This program is distributed in the hope that it will be useful, 
* but WITHOUT ANY WARRANTY; without even the implied warranty of 
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
* GNU General Public License for more details. 
*
* You should have received a copy of the GNU General Public License 
* along with this program. If not, see http://www.gnu.org/licenses
*/

package org.enhydra.shark.api.common;

import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;

/**
 * FilterBuilder interface helps building expressions for any BaseIterator implementation
 * or methods using filters as input parameter. Experience has learned us that it's not
 * that easy to build any useful expression to be used in xxIterators. Since Shark
 * supports BeanShell and JavaScript, making expressions starts to be even more
 * complicated once you start to use string literals. Also, reading and debugging of such
 * expression might turn into very tedious task. On the other side OMG (or at least the
 * way we read it) explicitely says set_expression method takes String as parameter, and
 * there's no escape. FilterBuilder and it's extending interfaces/classes serves the
 * intention to ease this task, although there is another benefit - it allows us to
 * prepare such expressions xxIterator can execute directly against database, thus
 * improving the performance.
 * 
 * @author Vladimir Puskas
 * @version 0.2
 */
public interface FilterBuilder {
   public static final int SQL_TYPE_ACTIVITY = 3;

   public static final int SQL_TYPE_PROCESS = 4;

   public static final int SQL_TYPE_MANAGER = 5;

   public static final int SQL_TYPE_RESOURCE = 6;

   public static final int SQL_TYPE_ASSIGNMENT = 7;

   public static final int SQL_TYPE_EVENT_AUDIT = 8;

   public static final int SQL_TYPE_VARIABLE = 9;

   public static final boolean ORDER_ASCENDING = true;

   public static final boolean ORDER_DESCENDING = false;

   public WMFilter and(WMSessionHandle sHandle, WMFilter f1, WMFilter f2) throws Exception;

   public WMFilter andForArray(WMSessionHandle sHandle, WMFilter[] fs) throws Exception;

   public WMFilter or(WMSessionHandle sHandle, WMFilter f1, WMFilter f2) throws Exception;

   public WMFilter orForArray(WMSessionHandle sHandle, WMFilter[] fs) throws Exception;

   public WMFilter not(WMSessionHandle sHandle, WMFilter f) throws Exception;

   public WMFilter setStartPosition(WMSessionHandle sHandle, WMFilter f, int startAt)
      throws Exception;

   public WMFilter setLimit(WMSessionHandle sHandle, WMFilter f, int limit) throws Exception;

   public WMFilter createEmptyFilter(WMSessionHandle shandle) throws Exception;

   /**
    * Appends arbitrary condition
    * <p>
    * Here you may specify any script compatible expression, but <b>beware complete
    * expression will be evaluated inside Java VM </b>, not on DB.
    */
   public WMFilter addBshExpression(WMSessionHandle sHandle, WMFilter filter, String exp)
      throws Exception;

   public String toIteratorExpression(WMSessionHandle sHandle, WMFilter filter) throws Exception;

   public String getIteratorEmptyExpression(WMSessionHandle sHandle) throws Exception;

}
