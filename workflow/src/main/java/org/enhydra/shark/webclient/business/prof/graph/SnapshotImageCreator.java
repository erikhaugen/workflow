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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.components.graph.Graph;
import org.enhydra.jawe.components.graph.GraphController;
import org.enhydra.jawe.components.graph.GraphControllerPanel;
import org.enhydra.jawe.components.graph.GraphUtilities;
import org.enhydra.jxpdl.elements.Activity;
import org.enhydra.jxpdl.elements.Package;
import org.enhydra.jxpdl.elements.WorkflowProcess;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMActivityInstance;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.AdminMisc;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.common.ActivityFilterBuilder;
import org.enhydra.shark.api.common.SharkConstants;
import org.enhydra.shark.utilities.interfacewrapper.SharkInterfaceWrapper;

/**
 * Captures the entire logic related with capturing the image snapshot of the current
 * state of the specified process, optionally performing coloring process in between.
 */
public final class SnapshotImageCreator {

   public static final String RUNNING_ACTIVITY_COLOR_KEY = "graph_activity_running_color";

   public static final String FINISHED_ACTIVITY_COLOR_KEY = "graph_activity_finished_color";

   public static final String PENDING_ACTIVITY_COLOR_KEY = "graph_activity_pending_color";

   public static final String NONEXECUTED_ACTIVITY_COLOR_KEY = "graph_activity_nonexecuted_color";

   protected static Color running_color;

   protected static Color finished_color;

   protected static Color pending_color;

   protected static Color nonexecuted_color;

   // synchronization aid
   private static final Object mutex = new Object();

   public static void init(WMSessionHandle shandle, Properties props) throws Exception {

      running_color=ColorManager.getColor(props.getProperty(RUNNING_ACTIVITY_COLOR_KEY, "R=255,G=255,B=128"),Color.green);
      finished_color=ColorManager.getColor(props.getProperty(FINISHED_ACTIVITY_COLOR_KEY, "R=255,G=128,B=128"),Color.red);
      pending_color=ColorManager.getColor(props.getProperty(PENDING_ACTIVITY_COLOR_KEY, "R=128,G=255,B=128"),Color.yellow);
      nonexecuted_color=ColorManager.getColor(props.getProperty(NONEXECUTED_ACTIVITY_COLOR_KEY, "R=255,G=255,B=255"),Color.white);
      JaWEUtil.init();
      JaWEUtil.refreshMainVersions(shandle);

   }

   /**
    * Result of this method is a byte[] of JPG picture representing process instance graph
    * with activities colored depending on their state, or process definition graph if
    * processId parameter is null
    */
   public static byte[] getGraph(WMSessionHandle shandle, String processId, String uniqueDefId)
      throws Exception {
      AdminMisc adminMisc = SharkInterfaceWrapper.getShark().getAdminMisc();
      WMEntity ent = null;
      if (processId != null) {
         ent = adminMisc.getProcessDefinitionInfo(shandle, processId);
      } else {
         ent = adminMisc.getProcessDefinitionInfoByUniqueProcessDefinitionName(shandle,
                                                                               uniqueDefId);
      }
      Package packag = JaWEManager.getInstance()
         .getXPDLHandler()
         .getPackageByIdAndVersion(ent.getPkgId(), ent.getPkgVer());

      if (packag == null) {
         JaWEUtil.insertPackage(shandle,ent.getPkgId(),ent.getPkgVer());
      }
      packag = JaWEManager.getInstance()
         .getXPDLHandler()
         .getPackageByIdAndVersion(ent.getPkgId(), ent.getPkgVer());

      if (packag == null) {
         throw new Exception("Didn't find package for a "
                             + ((processId != null) ? ("process " + processId)
                                                   : ("definition " + uniqueDefId))
                             + " !!!");
      }

      WorkflowProcess wp = packag.getWorkflowProcess(ent.getId());

      // The following map which contains last executed activity instance for each
      // activity definition
      Map relationMap = makeRelationMap(shandle, wp, processId);
      if (processId!=null) {
         makeColorMap(shandle, relationMap);
      }

      // graph must be retrieved after getting activity runtime information and
      // setting color map so everything is properly painted
      GraphController graphController = GraphUtilities.getGraphController();
      graphController.selectGraphForElement(wp);
      Graph graph = graphController.getGraph(wp);

      if (graph == null) {
         throw new Exception("Didn't find graph for a process " + processId + " !!!");
      }

      // this block has to be synchronized so each process gets painted differently
      BufferedImage img = null;
      synchronized (mutex) {
         ((GraphControllerPanel) graphController.getView()).graphSelected(graph);
         graph.setSize(graph.getPreferredSize());
         img = getJPGImage(graph);
      }

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ImageIO.write(img, "JPEG", bos);
      byte[] ba = bos.toByteArray();
      bos.close();
      return ba;
   }

   /**
    * Making map where one activity definitions point to the coresponding list of activity
    * instances.
    */
   protected static Map makeRelationMap(WMSessionHandle shandle,
                                 WorkflowProcess wp,
                                 String procId) throws Exception {

      Map relationMap = new HashMap();
      List activityDefinitions = wp.getActivities().toElements();
      for (Iterator it = activityDefinitions.iterator(); it.hasNext();) {
         Activity activity = (Activity) it.next();
         relationMap.put(activity.getId(), null);
      }

      if (procId != null) {
         WAPI wapi = SharkInterfaceWrapper.getShark().getWAPIConnection();
         ActivityFilterBuilder fb = SharkInterfaceWrapper.getShark()
            .getActivityFilterBuilder();

         WMFilter filter = fb.addProcessIdEquals(shandle, procId);
         WMActivityInstance[] activitiesInstances = wapi.listActivityInstances(shandle,
                                                                               filter,
                                                                               true)
            .getArray();

         // Filling relationMap with list of activity instances.
         for (int i = 0; i < activitiesInstances.length; i++) {
            WMActivityInstance wfActivity = activitiesInstances[i];
            String definitionId = wfActivity.getActivityDefinitionId();

            if (relationMap.containsKey(definitionId)) {
               WMActivityInstance act = (WMActivityInstance) relationMap.get(definitionId);

               if (act == null
                   || wfActivity.getState()
                      .stringValue()
                      .startsWith(SharkConstants.STATEPREFIX_OPEN)) {
                  relationMap.put(definitionId, wfActivity);
               }
            }
         }
      }

      return relationMap;
   }

   protected static void makeColorMap(WMSessionHandle shandle, Map relationMap) throws Exception {
      Map colorMap = new HashMap();
      if (relationMap != null) {
         Iterator it = relationMap.entrySet().iterator();
         while (it.hasNext()) {
            Map.Entry me = (Map.Entry) it.next();
            String actDefId = (String) me.getKey();
            WMActivityInstance act = (WMActivityInstance) relationMap.get(actDefId);
            Color c = null;
            if (act == null) {
               c = nonexecuted_color;
            } else {
               String st = act.getState().stringValue();
               if (st.startsWith(SharkConstants.STATEPREFIX_CLOSED)) {
                  c = finished_color;
               } else if (st.equals(SharkConstants.STATE_OPEN_RUNNING)) {
                  c = running_color;
               } else {
                  c = pending_color;
               }

            }
            colorMap.put(me.getKey(), c);
         }
      }
      SGraphActivityRenderer.putColorMap(colorMap);
      SGraphEventActivityRenderer.putColorMap(colorMap);
   }

   /**
    * Gets image of the process graph.
    */
   protected static BufferedImage getJPGImage(Graph graph) throws Exception {
      BufferedImage img = null;
      Object[] cells = graph.getRoots();
      graph.refresh();
      if (cells.length > 0) {
         Rectangle bounds = graph.getCellBounds(cells).getBounds();// HM, JGraph3.4.1
         graph.toScreen(bounds);

         // Create a Buffered Image
         Dimension d = bounds.getSize();
         img = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
         Graphics2D graphics = img.createGraphics();
         graph.paint(graphics);
      }

      return img;
   }

}
