<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
    Together Workflow Server
    Copyright (C) 2011 Together Teamsolutions Co., Ltd.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or 
    (at your option) any later version.
 
    This program is distributed in the hope that it will be useful, 
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program. If not, see http://www.gnu.org/licenses
-->

<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.Map"%>
<%@ page import="org.enhydra.shark.api.client.wfmodel.WfAssignment"%>
<%@ page import="org.enhydra.shark.api.client.wfservice.SharkConnection"%>
<%@ page import="org.enhydra.shark.jspclient.JSPClientUtilities"%>
<%@ page import="org.enhydra.shark.jspclient.VariableData"%>
<%@ page import="javax.transaction.*"%>
<html>
<head>
<title>Together Workflow Server 6.0-1 JSP Client</title>

<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<style>
   <!--
      .CyanBG { font-family:sans-serif; font-size:large; color:black; background-color:cyan;}
      .RedBG { font-family:sans-serif; font-size:large; color:black; background-color:red;}
      .BlueBG { font-family:sans-serif; font-size:large; color:black; background-color:#7777FF;}
      .PurpleBG { font-family:sans-serif; font-size:large; color:black; background-color:#AA44AA;}
      .YellowBG { font-family:sans-serif; font-size:large; color:black; background-color:yellow;}
      .GrayBG { font-family:sans-serif; font-size:large; color:black; background-color:gray;}
      .GreenBG { font-family:sans-serif; font-size:large; color:black; background-color:lightgreen;}
      a { color:black; text-decoration:none;}
      body { background-color:lightgrey;}
      tr { text-align:center;}
   -->
   </style>
</head>
<%!static final String FORM_METHOD = "post";

   static final String VARIABLESET = "variable_set";

   static final String COMPLETE = "complete";

   static final String ACCEPT = "accept";

   static final String OPEN_N_START = "openNstart";

   static final String LOAD_PACKAGE = "loadPackage";

   %>
<body>
<div align="center">
<h2>Together Workflow Server 6.0-1 JSP Client</h2>
<hr>

<table class="PurpleBG" width="65%" border="0" cellpadding="0"
	cellspacing="0">
	<%JSPClientUtilities.initProperties(getServletContext().getRealPath("/"));
         UserTransaction ut = null;
         try {
            ut = JSPClientUtilities.getUserTransaction();
            ut.begin();
            JSPClientUtilities.init();
            ut.commit();
         } catch (Exception ex) {
            try {
               if (ut.getStatus() != Status.STATUS_NO_TRANSACTION) {
                  ut.rollback();
               }
            } catch (Exception _) {
            }
            throw ex;
         }

         %>
</table>
</div>
<%SharkConnection sConn = null;
         try {
            ut.begin();
            sConn = JSPClientUtilities.connect();

            String activityId = request.getParameter("activity");
            String fn = request.getParameter("fn");
            if (null != fn) {
               if (fn.equals(COMPLETE))
                  JSPClientUtilities.activityComplete(sConn, activityId);
               else if (fn.equals(ACCEPT))
                  JSPClientUtilities.assignmentAccept(sConn, activityId);
               else if (fn.equals(VARIABLESET)) {
                  String vName = request.getParameter("varName");
                  String vValue = request.getParameter("varValue");
                  JSPClientUtilities.variableSet(sConn, activityId, vName, vValue);
               } else if (fn.equals(OPEN_N_START)) {
                  String vValue = request.getParameter("varValue");
                  JSPClientUtilities.processStart(sConn, vValue);
               } else if (fn.equals(LOAD_PACKAGE)) {
                  String vValue = request.getParameter("varValue");
                  JSPClientUtilities.packageLoad(sConn, vValue);
               }
            }
            ut.commit();
         } catch (Exception ex) {
            try {
               if (ut.getStatus() != Status.STATUS_NO_TRANSACTION) {
                  ut.rollback();
               }
            } catch (Exception _) {
            }
            throw ex;
         }
         try {
            // not accepted
            ut.begin();
            WfAssignment[] wItems = sConn.getResourceObject().get_sequence_work_item(0);
            if (null != wItems) {%>
<div align="center">
<table width="65%" border="0" cellpadding="0" cellspacing="0">
	<tr class="GrayBG">
		<td colspan="2" align="center">Task list for user admin</td>
	</tr>
	<%for (int i = 0; i < wItems.length; ++i) {
                  if (JSPClientUtilities.isMine(sConn, wItems[i])) {
                     continue;
                  }

                  %>
	<tr class="GreenBG">
		<td align="center" valign="top">
		<form name="activity_<%=wItems[i].activity().key()%>"
			method="<%=FORM_METHOD%>" action="">
		<p>Activity</p>
		<h3><%=wItems[i].activity().name()%></h3>
		<tt><%=wItems[i].activity().key()%></tt>
		<p><input type="hidden" name="activity"
			value="<%=wItems[i].activity().key()%>"> <input type="submit"
			name="fn" value="<%=ACCEPT%>">
		</form>
		<p></p>
		</td>
	</tr>
	<%}
            }
            // accepted
            wItems = sConn.getResourceObject().get_sequence_work_item(0);
            if (null != wItems) {
               for (int i = 0; i < wItems.length; ++i) {
                  if (!JSPClientUtilities.isMine(sConn, wItems[i])) {
                     continue;
                  }

                  %>
	<tr class="CyanBG">
		<td align="center" valign="top">
		<form name="activity_<%=wItems[i].activity().key()%>"
			method="<%=FORM_METHOD%>" action="">
		<p>Activity</p>
		<h3><%=wItems[i].activity().name()%></h3>
		<tt><%=wItems[i].activity().key()%></tt>
		<p><!--input type="hidden" name="fn" value="complete"--> <input
			type="hidden" name="activity" value="<%=wItems[i].activity().key()%>">
		<input type="submit" name="fn" value="<%=COMPLETE%>">
		</form>
		</td>
		<td><%for (Iterator it = JSPClientUtilities.getVariableData(sConn,wItems[i].activity())
                     .iterator(); it.hasNext();) {
                     VariableData vd = (VariableData) it.next();

                     %>
		<p><b><%=vd.getId()%>:</b>
		<form name="variable_<%=vd.getId()%>" method="<%=FORM_METHOD%>" action=""><input
			type="text" name="varValue"
			enabled="<%=vd.isToUpdate()%>"
			value="<%=vd.getVal()%>"
			onblur="submit();"> <input type="hidden"
			name="activity" value="<%=wItems[i].activity().key()%>"> <input
			type="hidden" name="fn" value="<%=VARIABLESET%>"> <input
			type="hidden" name="varName" value="<%=vd.getId()%>"></form>
		</p>
		<%}

               %></td>
	</tr>
	<%}

            %>
</table>
</div>
<%}
            ut.commit();
         } catch (Exception ex) {
            try {
               if (ut.getStatus() != Status.STATUS_NO_TRANSACTION) {
                  ut.rollback();
               }
            } catch (Exception _) {
            }
            throw ex;
         }
%>
<hr>
<div align="center">
<table>
	<tr>
		<td valign="top">
		<h4>Load package</h4>
		<form name="loadPackage" method="<%=FORM_METHOD%>" action=""><select
			name="varValue" size="6" onchange="submit();">
			<%String[] a = JSPClientUtilities.xpdlsToLoad();
         for (int i = 0; i < a.length; ++i) {

            %>
			<option value="<%=a[i]%>"><%=a[i]%></option>
			<%}

         %>
		</select> <input type="hidden" name="fn" value="<%=LOAD_PACKAGE%>"></form>
		</td>
		<td valign="top">
		<h4>Start process</h4>
		<form name="processStart" method="<%=FORM_METHOD%>" action=""><select
			name="varValue" size="8" onchange="submit();">
			<%try {
            ut.begin();
            String[] p = JSPClientUtilities.processesToStart(sConn);
            for (int i = 0; i < p.length; ++i) {

               %>
			<option value="<%=p[i]%>"><%=p[i]%></option>
			<%}
            ut.commit();
            JSPClientUtilities.disconnect(sConn);
         } catch (Exception ex) {
            try {
               if (ut.getStatus() != Status.STATUS_NO_TRANSACTION) {
                  ut.rollback();
               }
            } catch (Exception _) {
            }
            throw ex;
         }

         %>
		</select> <input type="hidden" name="fn" value="<%=OPEN_N_START%>"></form>
		</td>
	</tr>
</table>
</div>
<div align="center">
<form name="rForm" method="<%=FORM_METHOD%>" action=""><input
	type="submit" name="fn" value="refresh"></form>
</div>
<p align="left">&nbsp;</p>
</body>
</html>
