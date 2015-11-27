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

import java.io.File;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;
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
import org.enhydra.shark.api.internal.repositorypersistence.RepositoryPersistenceManager;
import org.enhydra.shark.utilities.interfacewrapper.SharkInterfaceWrapper;
import org.enhydra.shark.utilities.logging.LoggingUtilities;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author slavb
 */
@Component
public class MigrationUtils {

    static int instancesPerTransaction = 100;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MigrationUtils.class);

    public static Boolean updatePackage(UserTransaction ut, WMSessionHandle shandle, String pkgId,String filePath, boolean disablePrevManagers) throws Exception {
        PackageAdministration pa = SharkInterfaceWrapper.getShark().getPackageAdministration();
        WAPI wapi = SharkInterfaceWrapper.getShark().getWAPIConnection();
        
        try {
            Boolean result = false;
            ut.begin();
            String prevVersionInt = pa.getCurrentPackageVersion(shandle, pkgId);
            //byte[] utf8BytesOld = pa.getPackageContent(shandle, pkgId, prevVersionInt);
            RepositoryPersistenceManager rpm = (RepositoryPersistenceManager) SharkInterfaceWrapper.getShark()
                    .getPlugIn(SharkConstants.PLUGIN_REPOSITORY_PERSISTENCE_MANAGER);
            long uploadTime = rpm.getXPDLUploadTime(shandle, pkgId, prevVersionInt);
            File file = new File(filePath);
            if (file.lastModified() > uploadTime) {
                //byte[] utf8BytesNew = MiscUtilities.convertFileToByteArray(filePath);
                //pa.updatePackage(shandle, pkgId, utf8BytesNew);
                pa.updatePackageFromFile(shandle, pkgId, filePath);
                if (disablePrevManagers) {
                    //disable all previous versions managers
                    String curVersionInt = pa.getCurrentPackageVersion(shandle, pkgId);
                    WMProcessDefinition[] mgrs = getPreviosProcessDefinitions(shandle, pkgId, curVersionInt);
                    if (mgrs != null) {
                        for (WMProcessDefinition mgr : mgrs) {
                            wapi.changeProcessDefinitionState(shandle, mgr.getName(), WMProcessDefinitionState.DISABLED);
                        }
                    }
                }
                result = true;

            }
            ut.commit();
            return result;
        } finally {
            DBUtils.rollback(ut);
        }

    }
/*
    private static boolean isPackageChanged(byte[] utf8BytesOld, byte[] utf8BytesNew) {
        XMLSource xsOld = new XMLSource(new ByteArrayInputStream(utf8BytesOld));
        XMLSource xsNew = new XMLSource(new ByteArrayInputStream(utf8BytesNew));
        Map pfx = new HashMap<String, String>() {
            {
                put("xpdl", "http://www.wfmc.org/2008/XPDL2.1");
            }
        };
        String versionOld = xsOld.getValue("/xpdl:Package/xpdl:RedefinableHeader/xpdl:Version", pfx);
        String versionNew = xsNew.getValue("/xpdl:Package/xpdl:RedefinableHeader/xpdl:Version", pfx);
        return !versionOld.equals(versionNew);
    }*/

    public static void migrateAllPackages() {
        UserTransaction ut = null;
        try {
            javax.naming.Context ctx = new InitialContext();
            ut = (UserTransaction) ctx.lookup("java:comp/env/UserTransaction");
            ut.begin();
            WMSessionHandle shandle = SharkInterfaceWrapper.getDefaultSessionHandle(null);
            PackageAdministration pa = SharkInterfaceWrapper.getShark().getPackageAdministration();
            String[] pkgIds = pa.getOpenedPackageIds(shandle);
            ut.commit();
            for (String pkgId : pkgIds) {
                migratePackage(ut, shandle, pkgId);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            DBUtils.rollback(ut);
        }
    }

    public static void migratePackage(UserTransaction ut, WMSessionHandle shandle, String pkgId) throws Exception {
        PackageAdministration pa = SharkInterfaceWrapper.getShark().getPackageAdministration();
        WAPI wapi = SharkInterfaceWrapper.getShark().getWAPIConnection();
        try {
            ut.begin();
            String curVersionInt = pa.getCurrentPackageVersion(shandle, pkgId);
            WMProcessDefinition[] mgrs = getPreviosProcessDefinitions(shandle, pkgId, curVersionInt);
            ut.commit();
            if (mgrs != null) {
                for (WMProcessDefinition mgr : mgrs) {
                    try {
                        migrate(ut, shandle, mgr, curVersionInt);
                    } catch (Exception e) {
                        logger.error("Migration of manager " + mgr.getName() + " failed", e);
                    }
                }
            }
        } finally {
            DBUtils.rollback(ut);
        }

    }

    private static void migrate(UserTransaction ut, WMSessionHandle shandle, WMProcessDefinition mgr, String newVersion) throws Exception {
        ProcessFilterBuilder pfb = SharkInterfaceWrapper.getShark().getProcessFilterBuilder();
        WAPI wapi = SharkInterfaceWrapper.getShark().getWAPIConnection();
        try {
            ut.begin();
            WMFilter processfilter = pfb.addStateStartsWith(shandle, SharkConstants.STATEPREFIX_OPEN);
            processfilter = pfb.and(shandle, processfilter, pfb.addMgrNameEquals(shandle, mgr.getName()));
            //processfilter = pfb.and(shandle, processfilter, pfb.addIdEquals(shandle, "6301_offer_offer_grace"));
            WMProcessInstance[] procs = wapi.listProcessInstances(shandle, processfilter, false).getArray();

            int i = 0;
            for (WMProcessInstance proc : procs) {
                try {
                    if (i % instancesPerTransaction == 0) {
                        logger.info("commit work int");
                        ut.commit();
                        ut.begin();
                    }
                    migrate(ut, shandle, proc, newVersion);
                    i++;
                } catch (Exception ex) {
                    logger.error("Migration process " + proc.getId() + " failed", ex);
                }
            }
            logger.info("commit work end");
            ut.commit();

        } finally {
            DBUtils.rollback(ut);
        }

    }

    private static void migrate(UserTransaction ut, WMSessionHandle shandle, WMProcessInstance proc, String newVersion) throws Exception {
        WAPI wapi = SharkInterfaceWrapper.getShark().getWAPIConnection();
        WMProcessDefinition mgr = wapi.getProcessDefinition(shandle, proc.getProcessFactoryName());
        LoggingUtilities.log4jWarn(null, "Migrating " + proc.getId() + " " + mgr.getVersion() + " -> " + newVersion, null, true, true);

        String factoryId = mgr.getPackageId() + "#" + newVersion + "#" + mgr.getId();
        SharkInterfaceWrapper.getShark().getExecutionAdministrationExtension()
                .migrateProcessVersion(shandle, factoryId, proc.getId());

    }

    private static WMProcessDefinition[] getPreviosProcessDefinitions(WMSessionHandle shandle, String pkgId, String curVersionInt) throws Exception {
        PackageAdministration pa = SharkInterfaceWrapper.getShark().getPackageAdministration();
        WAPI wapi = SharkInterfaceWrapper.getShark().getWAPIConnection();

        ProcessMgrFilterBuilder fb = SharkInterfaceWrapper.getShark().getProcessMgrFilterBuilder();
        WMFilter filter = fb.and(shandle,
                fb.addPackageIdEquals(shandle, pkgId),
                fb.not(shandle, (fb.addVersionEquals(shandle, curVersionInt)))
        );
        WMProcessDefinition[] mgrs = wapi.listProcessDefinitions(shandle, filter, true).getArray();

        return mgrs;

    }

}
