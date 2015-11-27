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

import java.net.URI;
import java.util.concurrent.Callable;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMProcessInstance;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.ExecutionAdministration;
import org.enhydra.shark.api.common.ProcessFilterBuilder;
import org.enhydra.shark.api.common.SharkConstants;
import org.enhydra.shark.utilities.interfacewrapper.SharkInterfaceWrapper;
import org.quartz.JobExecutionException;
import org.springframework.transaction.annotation.Transactional;
import ru.ilb.common.async.TaskManager;
import ru.ilb.workflow.api.AdminResource;
import ru.ilb.workflow.api.ProcessesResource;
import ru.ilb.workflow.job.CheckDeadlines;
import ru.ilb.workflow.job.ReevaluateAssignments;
import ru.ilb.workflow.utils.MigrationUtils;

@Path("admin")
public class AdminResourceImpl implements AdminResource {

    private UriInfo uriInfo;
    private HttpServletRequest httpServletRequest;
    private HttpHeaders headers;

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

    @Context
    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    @Transactional
    public Response reevaluateAssignments() {
        final URI uri = uriInfo.getBaseUriBuilder().path(ProcessesResource.class).path(ProcessesResource.class, "getWorkList").build();;
        return TaskManager.execute(new Callable() {
            @Override
            public Object call() throws Exception {
                reevaluateAssignmentsInt();
                return Response.seeOther(uri).build();
            }
        }, uriInfo);

    }

    public void reevaluateAssignmentsInt() {
        ReevaluateAssignments task = new ReevaluateAssignments();
        try {
            task.execute(null);
        } catch (JobExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    @Transactional
    public String clearProcessCache() {
        try {
            WMSessionHandle shandle = SharkInterfaceWrapper.getSessionHandle(getUsername(), null);
            SharkInterfaceWrapper.getShark()
                    .getExecutionAdministration().clearProcessCache(shandle);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return "ok";
    }

    @Override
    @Transactional
    public String clearResourceCache() {
        try {
            WMSessionHandle shandle = SharkInterfaceWrapper.getSessionHandle(getUsername(), null);
            SharkInterfaceWrapper.getShark()
                    .getExecutionAdministration().clearResourceCache(shandle);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return "ok";
    }

    @Override
    //@Transactional
    public Response checkDeadlines() {
        final URI uri = uriInfo.getBaseUriBuilder().path(ProcessesResource.class).path(ProcessesResource.class, "getWorkList").build();;
        return TaskManager.execute(new Callable() {
            @Override
            public Object call() throws Exception {
                checkDeadlinesInt();
                return Response.seeOther(uri).build();
            }
        }, uriInfo);
    }

    //@Transactional

    public void checkDeadlinesInt() {
        CheckDeadlines task = new CheckDeadlines();
        try {
            task.execute(null);
        } catch (JobExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Response migrateProcesses() {
        return TaskManager.execute(new Callable() {
            @Override
            public Object call() throws Exception {
                MigrationUtils.migrateAllPackages();
                return "ok";
            }
        }, uriInfo);
    }

    @Override
    public Response reevaluateDeadlines() {
        return TaskManager.execute(new Callable() {
            @Override
            public Object call() throws Exception {
                return reevaluateDeadlinesInt();
            }
        }, uriInfo);
    }

    private String reevaluateDeadlinesInt() {
        UserTransaction ut = null;
        try {
            javax.naming.Context ctx = new InitialContext();
            ut = (UserTransaction) ctx.lookup("java:comp/env/UserTransaction");
            ut.begin();
            WAPI wapi = SharkInterfaceWrapper.getShark().getWAPIConnection();
            ExecutionAdministration executionAdministration = SharkInterfaceWrapper.getShark().getExecutionAdministration();
            WMSessionHandle shandle = SharkInterfaceWrapper.getDefaultSessionHandle(null);
            ProcessFilterBuilder pfb = SharkInterfaceWrapper.getShark().getProcessFilterBuilder();
            WMFilter filter = pfb.addStateStartsWith(shandle, SharkConstants.STATEPREFIX_OPEN);
            WMProcessInstance[] procs = wapi.listProcessInstances(shandle, filter, false).getArray();
            ut.commit();
            if (procs != null) {
                for (WMProcessInstance proc : procs) {
                    ut.begin();
                    executionAdministration.reevaluateDeadlinesForProcesses(shandle, new String[]{proc.getId()});
                    ut.commit();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                if (ut != null && ut.getStatus() != Status.STATUS_NO_TRANSACTION) {
                    ut.rollback();
                }
            } catch (SystemException | IllegalStateException | SecurityException ex1) {
            }
        }
        return "ok";
    }

}
