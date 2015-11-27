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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.cxf.jaxrs.ext.xml.XMLSource;
import org.enhydra.shark.admin.repositorymanagement.RepositoryManager;
import org.enhydra.shark.api.admin.RepositoryMgr;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMProcessDefinition;
import org.enhydra.shark.api.client.wfmc.wapi.WMProcessDefinitionState;
import org.enhydra.shark.api.client.wfmc.wapi.WMProcessInstance;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.PackageAdministration;
import org.enhydra.shark.api.common.ProcessFilterBuilder;
import org.enhydra.shark.api.common.ProcessMgrFilterBuilder;
import org.enhydra.shark.api.common.SharkConstants;
import org.enhydra.shark.utilities.interfacewrapper.SharkInterfaceWrapper;
import org.enhydra.shark.utilities.logging.LoggingUtilities;
import org.enhydra.shark.utilities.misc.MiscUtilities;
import org.slf4j.LoggerFactory;
import ru.ilb.common.async.TaskManager;
import ru.ilb.workflow.api.ProcessesResource;
import ru.ilb.workflow.api.XpdlrepositoryResource;
import ru.ilb.workflow.utils.DBUtils;
import ru.ilb.workflow.utils.MigrationUtils;

@Path("xpdlrepository")
public class XpdlrepositoryResourceImpl implements XpdlrepositoryResource {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(XpdlrepositoryResourceImpl.class);

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

    @Override
    public Response loadAll(final Boolean migrate) {
        return TaskManager.execute(new Callable() {
            @Override
            public Object call() throws Exception {
                return loadAllInt(migrate);
            }
        }, uriInfo);
    }

    public String loadAllInt(Boolean migrate) {
        UserTransaction ut = null;
        try {
            javax.naming.Context ctx = new InitialContext();
            ut = (UserTransaction) ctx.lookup("java:comp/env/UserTransaction");
            ut.begin();

            RepositoryMgr rm = RepositoryManager.getInstance();
            Map ppm = rm.getPackagePathToIdMapping();
            PackageAdministration pa = SharkInterfaceWrapper.getShark().getPackageAdministration();
            String repoPath = rm.getPathToXPDLRepositoryFolder();

            WMSessionHandle shandle = SharkInterfaceWrapper.getDefaultSessionHandle(null);
            WAPI wapi = SharkInterfaceWrapper.getShark().getWAPIConnection();

            List opids = Arrays.asList(pa.getOpenedPackageIds(shandle));
            Iterator itt = ppm.entrySet().iterator();
            ut.commit();
            while (itt.hasNext()) {
                Map.Entry me = (Map.Entry) itt.next();
                String pkgId = (String) me.getValue();
                String filePath = repoPath + File.separator + (String) me.getKey();
                if (!opids.contains(pkgId)) {
                    ut.begin();
                    pa.openPackage(shandle, filePath);
                    ut.commit();
                } else {
                    try {
                        
                        if (MigrationUtils.updatePackage(ut, shandle, pkgId, filePath, true)) {
                            if (migrate) {
                                MigrationUtils.migratePackage(ut, shandle, pkgId);
                            }
                        }
                    } catch (Exception e) {
                        LoggingUtilities.log4jError(null, "Update of package " + pkgId + " failed", e, true, true);
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            DBUtils.rollback(ut);
        }
        return "ok";
    }


}
