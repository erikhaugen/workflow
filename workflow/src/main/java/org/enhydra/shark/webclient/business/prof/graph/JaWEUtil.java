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

package org.enhydra.shark.webclient.business.prof.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.XPDLElementChangeInfo;
import org.enhydra.jawe.components.graph.GraphUtilities;
import org.enhydra.jxpdl.XMLElementChangeInfo;
import org.enhydra.jxpdl.elements.Package;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.utilities.interfacewrapper.SharkInterfaceWrapper;
import org.enhydra.shark.utilities.wmentity.WMEntityUtilities;
import org.enhydra.shark.webclient.spec.Log;

/**
 * Maintains JaWE's XPDL model so it contains the same XPDL files as the ones uploaded to
 * Shark engine.
 */
public class JaWEUtil {

   private static ArrayList openedPackages = new ArrayList();

   /**
    * Configures and initializes JaWE.
    */
   public static synchronized void init() {
      JaWEStarter.configure();
      JaWEStarter.getInstance().init();
   }

   /**
    * Refreshes JaWEs list of opened packages by querying shark.
    */
   public static synchronized void refresh(WMSessionHandle shandle) throws Exception {
      // logger.info("JaWEUtil -> Refreshing list of uploaded packages.");
      try {
         ArrayList oldOML = new ArrayList(openedPackages);
         openedPackages.clear();
         openedPackages.addAll(Arrays.asList(WMEntityUtilities.getAllPackages(shandle,
                                                                              SharkInterfaceWrapper.getShark()
                                                                                 .getXPDLBrowser())));
         ArrayList toAdd = new ArrayList(openedPackages);
         toAdd.removeAll(oldOML);
         ArrayList toRemove = new ArrayList(oldOML);
         toRemove.removeAll(openedPackages);
         // logger.debug(".... inserting packages " + toAdd);
         insertPackages(shandle, toAdd);
         // logger.debug(".... removing packages " + toRemove);
         removePackages(toRemove);
      } catch (Exception ex) {
         // logger.error("JaWEUtil -> failed to refresh list of uploaded
         // packages.", ex);
         throw ex;
      }

   }

   public static synchronized void refreshMainVersions(WMSessionHandle shandle)
      throws Exception {
      // logger.info("JaWEUtil -> Refreshing list of uploaded packages.");
      try {
         String[] pkgIds = SharkInterfaceWrapper.getShark()
            .getPackageAdministration()
            .getOpenedPackageIds(shandle);
         if (pkgIds != null && pkgIds.length > 0) {
            List l = new ArrayList();
            for (int i = 0; i < pkgIds.length; i++) {
               String pkgVer = SharkInterfaceWrapper.getShark()
                  .getPackageAdministration()
                  .getCurrentPackageVersion(shandle, pkgIds[i]);
               l.add(SharkInterfaceWrapper.getShark()
                  .getPackageAdministration()
                  .getPackageEntity(shandle, pkgIds[i], pkgVer));

            }
            insertPackages(shandle, l);
         }
      } catch (Exception ex) {
         // logger.error("JaWEUtil -> failed to refresh list of uploaded
         // packages.", ex);
         throw ex;
      }

   }

   public static synchronized void insertPackage(WMSessionHandle shandle,
                                                 String pkgId,
                                                 String pkgVer) throws Exception {

      if (JaWEManager.getInstance()
         .getXPDLHandler()
         .getPackageByIdAndVersion(pkgId, pkgVer) != null) {
         return;
      }
      try {
         List inserted = new ArrayList();

         insertPackage2(shandle, pkgId, pkgVer, inserted);

         Iterator it = inserted.iterator();
         while (it.hasNext()) {
            Package pkg = (Package) it.next();
            JaWEManager.getInstance().getJaWEController().adjustXPDL(pkg);
            pkg.setReadOnly(true);
         }

         if (inserted.size() > 0) {
            XPDLElementChangeInfo info = JaWEManager.getInstance()
               .getJaWEController()
               .createInfo((Package) inserted.get(0),
                           inserted,
                           XMLElementChangeInfo.INSERTED);
            GraphUtilities.getGraphController().update(null, info);
            JaWEManager.getInstance()
               .getJaWEController()
               .getSelectionManager()
               .setSelection((Package) inserted.get(0), true);
         }
      } catch (Exception ex) {
         Log.logException("Problems while inserting package [Id="
                          + pkgId + ",Ver=" + pkgVer + "]", ex);
         throw ex;
      }

   }

   protected static void insertPackage2(WMSessionHandle shandle,
                                        String pkgId,
                                        String pkgVer,
                                        List inserted) throws Exception {

      if (JaWEManager.getInstance()
         .getXPDLHandler()
         .getPackageByIdAndVersion(pkgId, pkgVer) != null) {
         return;
      }
      try {
         Log.log("Inserting package " + pkgId + ", version " + pkgVer);
         byte[] fileCont = SharkInterfaceWrapper.getShark()
            .getPackageAdministration()
            .getPackageContent(shandle, pkgId, pkgVer);
         Package pkg = JaWEManager.getInstance()
            .getXPDLHandler()
            .openPackageFromStream(fileCont, true);
         pkg.setInternalVersion(pkgVer);
         inserted.add(pkg);

         Iterator it = pkg.getExternalPackageIds().iterator();
         while (it.hasNext()) {
            String ePkgId = (String) it.next();
            insertPackage2(shandle, ePkgId, SharkInterfaceWrapper.getShark()
               .getPackageAdministration()
               .getCurrentPackageVersion(shandle, ePkgId), inserted);
         }
         Log.log("Package " + pkg.getId() + " version " + pkgVer + " inserted");
      } catch (Exception ex) {
         Log.logException("Problems while inserting package [Id="
                          + pkgId + ",Ver=" + pkgVer + "]", ex);
         throw ex;
      }

   }

   /**
    * Inserting XPDLs (that are uploaded into shark) into JaWE.
    */
   protected static void insertPackages(WMSessionHandle shandle, Collection ents)
      throws Exception {
      List inserted = new ArrayList();

      Iterator it = ents.iterator();
      while (it.hasNext()) {
         WMEntity ent = (WMEntity) it.next();
         try {
            byte[] fileCont = SharkInterfaceWrapper.getShark()
               .getPackageAdministration()
               .getPackageContent(shandle, ent.getId(), ent.getPkgVer());
            Package pkg = JaWEManager.getInstance()
               .getXPDLHandler()
               .openPackageFromStream(fileCont, true);
            pkg.setInternalVersion(ent.getPkgVer());
            inserted.add(pkg);
         } catch (Exception ex) {
            Log.logException("Problems while inserting package [Id="
                             + ent.getId() + ",Ver=" + ent.getPkgVer() + "]", ex);
            // logger.error("Failed to insert package [Id="
            // + ent.getId() + ",Ver=" + ent.getPkgVer()
            // + "] from JaWE's XPDL list", ex);
            throw ex;
         }
      }

      it = inserted.iterator();
      while (it.hasNext()) {
         Package pkg = (Package) it.next();
         JaWEManager.getInstance().getJaWEController().adjustXPDL(pkg);
         pkg.setReadOnly(true);
      }

      if (inserted.size() > 0) {
         XPDLElementChangeInfo info = JaWEManager.getInstance()
            .getJaWEController()
            .createInfo((Package) inserted.get(0),
                        inserted,
                        XMLElementChangeInfo.INSERTED);
         GraphUtilities.getGraphController().update(null, info);
         JaWEManager.getInstance()
            .getJaWEController()
            .getSelectionManager()
            .setSelection((Package) inserted.get(0), true);
      }

   }

   /**
    * Removing XPDLs (that are removed from shark) from JaWE's XPDL list.
    */
   protected static void removePackages(Collection ents) throws Exception {
      try {
         Iterator it = ents.iterator();
         List removed = new ArrayList();
         while (it.hasNext()) {
            WMEntity ent = (WMEntity) it.next();
            removed.add(JaWEManager.getInstance()
               .getXPDLHandler()
               .closePackageVersion(ent.getId(), ent.getPkgVer()));
         }
         if (removed.size() > 0) {
            XPDLElementChangeInfo info = JaWEManager.getInstance()
               .getJaWEController()
               .createInfo((Package) removed.get(0),
                           removed,
                           XMLElementChangeInfo.REMOVED);
            GraphUtilities.getGraphController().update(null, info);
         }
      } catch (Exception ex) {
         // logger.error("JaWEUtil -> Failed to remove packages "
         // + ents + " from JaWE's XPDL list", ex);
         throw ex;
      }
   }

}
