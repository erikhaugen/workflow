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

import com.lutris.logging.Logger;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.AdminMisc;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.utilities.interfacewrapper.SharkInterfaceWrapper;
import org.enhydra.shark.utilities.wmentity.WMEntityUtilities;

/**
 *
 * @author slavb
 */
public class SharkUtils {
   public static String getEAValue(WMSessionHandle shandle, String ea, String procDefId, String procId, String actId, String defaultValue) {
      try {
         String cval = null;

         AdminMisc am = SharkInterfaceWrapper.getShark().getAdminMisc();

         String pkgId = null;
         String pkgVer = null;
         if (null != actId && !actId.equals("")) {
            WMEntity actInfo = am.getActivityDefinitionInfo(shandle, procId, actId);
            pkgId = actInfo.getPkgId();
            pkgVer = actInfo.getPkgVer();
            cval = WMEntityUtilities.findEAAndGetValue(shandle, SharkInterfaceWrapper.getShark().getXPDLBrowser(), actInfo, ea);
         }
         if (cval == null || cval.trim().equals("")) {
            WMEntity procInfo = null;
            if (procId != null) {
               procInfo = am.getProcessDefinitionInfo(shandle, procId);
            } else {
               procInfo = am.getProcessDefinitionInfoByUniqueProcessDefinitionName(shandle, procDefId);
            }
            pkgId = procInfo.getPkgId();
            pkgVer = procInfo.getPkgVer();
            cval = WMEntityUtilities.findEAAndGetValue(shandle, SharkInterfaceWrapper.getShark().getXPDLBrowser(), procInfo, ea);

         }
         if (cval == null || cval.trim().equals("")) {
            WMEntity pkgInfo = SharkInterfaceWrapper.getShark().getPackageAdministration().getPackageEntity(shandle, pkgId, pkgVer);
            cval = WMEntityUtilities.findEAAndGetValue(shandle, SharkInterfaceWrapper.getShark().getXPDLBrowser(), pkgInfo, ea);
         }
         if (cval != null && !cval.trim().equals("")) {
            return cval;
         }
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }
      return defaultValue;
   }

}
