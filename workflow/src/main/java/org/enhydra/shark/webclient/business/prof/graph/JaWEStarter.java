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

import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.Utils;
import org.enhydra.jawe.XPDLUtils;
import org.enhydra.jawe.base.controller.ControllerSettings;
import org.enhydra.jawe.base.controller.JaWEController;
import org.enhydra.jawe.base.display.StandardDisplayNameGenerator;
import org.enhydra.jawe.base.idfactory.IdFactory;
import org.enhydra.jawe.base.label.StandardLabelGenerator;
import org.enhydra.jawe.base.logger.LoggingManager;
import org.enhydra.jawe.base.panel.InlinePanel;
import org.enhydra.jawe.base.panel.StandardPanelGenerator;
import org.enhydra.jawe.base.panel.StandardPanelValidator;
import org.enhydra.jawe.base.tooltip.StandardTooltipGenerator;
import org.enhydra.jawe.base.transitionhandler.TransitionHandler;
import org.enhydra.jawe.base.xpdlobjectfactory.XPDLObjectFactory;
import org.enhydra.jawe.base.xpdlvalidator.XPDLValidatorSettings;
import org.enhydra.jxpdl.StandardPackageValidator;
import org.enhydra.jxpdl.XPDLRepositoryHandler;
import org.enhydra.shark.webclient.spec.Log;

public class JaWEStarter extends JaWEManager {

   protected JaWEStarter() {
   }

   public static JaWEManager getInstance() {
      if (jaweManager == null) {
         jaweManager = new JaWEStarter();
      }

      return jaweManager;
   }

   public void init() {
      if (!isConfigured) {
         return;
      }

      panelGeneratorClassName = StandardPanelGenerator.class.getName();
      inlinePanelClassName = InlinePanel.class.getName();

      try {
         xpdlUtils = new XPDLUtils();
         loggingManager = new LoggingManager();
         XPDLRepositoryHandler xpdlRH = new XPDLRepositoryHandler();
         xpdlHandler = createXPDLHandler(xpdlRH);
         jaweController = new JaWEController(new ControllerSettings() {
            public boolean useJaWEFrame() {
               return false;
            }
         });
         jaweController.init();
         labelGenerator = new StandardLabelGenerator();
         transitionHandler = new TransitionHandler();
         idFactory = new IdFactory();
         xpdlObjectFactory = new XPDLObjectFactory();
         panelValidator = new StandardPanelValidator();
         XPDLValidatorSettings vs = new XPDLValidatorSettings();
         vs.init(null);
         xpdlValidator = new StandardPackageValidator(vs.getProperties());
         displayNameGenerator = new StandardDisplayNameGenerator();
         tooltipGenerator = new StandardTooltipGenerator();
         componentManager = new JaWECManager();
         componentManager.init();
         Utils.getActivityIconsMap();
      } catch (Throwable ex) {
         Log.logException(ex);
         String msg = "JaweManager -> problems starting jawe!";
         throw new Error(msg, ex);
      }
   }

   public String getName() {
      return "TWE";
   }
}
