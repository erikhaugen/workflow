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

package ru.ilb.workflow.web;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import org.enhydra.shark.api.admin.UserGroupManagerAdmin;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.utilities.interfacewrapper.SharkInterfaceWrapper;
import org.springframework.transaction.annotation.Transactional;

/**
 * REST Web Service
 *
 * @author slavb
 */
@Path("test")
public class TestResource {

    private UriInfo uriInfo;
    private HttpServletRequest httpServletRequest;

    @Context
    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Context
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public String getUsername() {
        return httpServletRequest.getRemoteUser();
    }

    @GET
    @Path("usergroup")
    @Produces("text/plain; charset=utf-8")
    @Transactional
    public String usergroup() {

        UserGroupManagerAdmin ugma = SharkInterfaceWrapper.getUserGroupAdmin();
        try {
            WMSessionHandle shandle = SharkInterfaceWrapper.getSessionHandle(getUsername(), null);

            String res = "getUserFirstName(): " + ugma.getUserFirstName(shandle, getUsername());
            res = res + "\n" + "getUserLastName(): " + ugma.getUserLastName(shandle, getUsername());
            res = res + "\n" + "getUserRealName(): " + ugma.getUserRealName(shandle, getUsername());
            res = res + "\n" + "getUserAttribute(getUsername(),uidNumber): " + ugma.getUserAttribute(shandle, getUsername(), "uidNumber");
            res = res + "\n" + "doesUserBelongToGroup(ru.bystrobank.tws.admins): " + ugma.doesUserBelongToGroup(shandle, "ru.bystrobank.tws.admins", getUsername());
            res = res + "\n" + "doesGroupExist(ru.bystrobank.tws.admins): " + ugma.doesGroupExist(shandle, "ru.bystrobank.tws.admins");
            res = res + "\n" + "getGroupDescription(ru.bystrobank.tws.admins): " + ugma.getGroupDescription(shandle, "ru.bystrobank.tws.admins");
            res = res + "\n" + "getGroupAttribute(ru.bystrobank.tws.admins,gidNumber): " + ugma.getGroupAttribute(shandle, "ru.bystrobank.tws.admins", "gidNumber");

            String[] entries;
            entries = ugma.getAllGroups(shandle);
            res = res + "\n" + "getAllGroups():" + entries.length;
//            for (String e : entries) {
//                res = res + " " + e;
//            }

            entries = ugma.getAllGroupsForUser(shandle, getUsername());
            res = res + "\n" + "getAllGroupsForUser(getUsername()):" + entries.length;
//            for (String e : entries) {
//                res = res + " " + e;
//            }

            entries = ugma.getAllUsers(shandle);
            res = res + "\n" + "getAllUsers:" + entries.length;
//            for (String e : entries) {
//                res = res + " " + e;
//            }

            entries = ugma.getAllUsersForGroups(shandle, new String[]{"ru.bystrobank.tws.admins"});
            res = res + "\n" + "getAllUsersForGroups(ru.bystrobank.tws.admins):" + entries.length;
            for (String e : entries) {
                res = res + " " + e;
            }

            // TODO остальные get-методы UserGroupManagerAdmin не используются либо не реализованы ( в т.ч. с Immediate и Subgroups )
            return res;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    @GET
    @Path("testuser")
    @Produces("text/plain; charset=utf-8")
    @Transactional
    public String testuser(@QueryParam("processId") String processId, @QueryParam("activityId") String activityId) {
        try {
            WMSessionHandle shandle = SharkInterfaceWrapper.getSessionHandle(getUsername(), null);
            String username=SharkInterfaceWrapper.getShark().getAdminMisc().getActivityResourceUsername(shandle, processId, activityId);
            Object prev=SharkInterfaceWrapper.getShark().getAdminMiscExtension().getPrevious(shandle, processId, activityId, true);

            return "User: "+username;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
