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

import java.awt.Graphics;
import java.awt.Rectangle;

import org.enhydra.jawe.components.graph.DefaultGraphArtifactRenderer;
import org.enhydra.jawe.components.graph.GraphArtifactInterface;
import org.enhydra.jxpdl.XPDLConstants;
import org.enhydra.jxpdl.elements.Artifact;

public class SGraphArtifactRenderer extends DefaultGraphArtifactRenderer {

   public void paint(Graphics g) {
      GraphArtifactInterface gact = (GraphArtifactInterface) view.getCell();
      Artifact art = (Artifact) gact.getUserObject();
      String type = art.getArtifactType();
      if (type.equals(XPDLConstants.ARTIFACT_TYPE_ANNOTATION)) {
         setBounds(g.getClipBounds());
      }
      super.paint(g);
   }

   public void setBounds(Rectangle rect) {
      if (split != null) {
         int iconSize = 0;

         mainIcon.setBounds(0, 0, 0, 0);
         name.setBounds(2, 2, rect.width - 2, rect.height - 3);
         split.setBounds(rect);
      }
   }

}
