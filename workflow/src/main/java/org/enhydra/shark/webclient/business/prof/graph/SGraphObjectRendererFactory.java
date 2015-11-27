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

import org.enhydra.jawe.components.graph.GraphActivityRendererInterface;
import org.enhydra.jawe.components.graph.GraphArtifactRendererInterface;
import org.enhydra.jawe.components.graph.GraphObjectRendererFactory;
import org.enhydra.jxpdl.XPDLConstants;
import org.enhydra.jxpdl.elements.Activity;
import org.enhydra.jxpdl.elements.Artifact;

public class SGraphObjectRendererFactory extends GraphObjectRendererFactory {

   public GraphActivityRendererInterface createActivityRenderer (Activity act) {
      int actType = act.getActivityType();
      if (actType == XPDLConstants.ACTIVITY_TYPE_EVENT_START
          || actType == XPDLConstants.ACTIVITY_TYPE_EVENT_END) {
         return new SGraphEventActivityRenderer();
      }
//      return new DefaultGraphActivityRenderer();
      return new SGraphActivityRenderer();
   }

   public GraphArtifactRendererInterface createArtifactRenderer(Artifact art) {
      return new SGraphArtifactRenderer();
   }
   
}
