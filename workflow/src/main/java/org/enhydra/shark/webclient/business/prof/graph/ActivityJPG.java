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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JTextArea;

import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.client.wfservice.XPDLBrowser;
import org.enhydra.shark.utilities.interfacewrapper.SharkInterfaceWrapper;

/**
 * ActivityJPG class represents activity. It extends JTextArea that enables text passed
 * text to be written on the symbol. JTextArea allows text wrapping.
 * 
 * @author Zorica Dudarin
 */
public class ActivityJPG extends JTextArea {
   private Properties props = null;

   /**
    * Activities name. Possibly walues are finished, pending and nonexecuted Activities
    * lauout depends on activities name
    */
   private String name = null;

   /**
    * Activities position on the graph
    */
   private Point pos = null;

   /**
    * List of destinatin activities. Activity should be linked with those activities.
    */
   private List destinationActivities = null;

   boolean drawRect = false;

   /**
    * Constructor
    * 
    * @param props - object with graph/activities settings
    * @param name - activities name
    * @param text - text that will be written on the symbol
    * @param pos - activities position on the graph
    */
   public ActivityJPG(Properties props, String name, WMEntity act, Point pos) {
      this.name = name;
      this.props = props;
      this.pos = pos;

      WMFilter filter = new WMFilter("Name", WMFilter.EQ, "ActivityTypes");
      filter.setFilterType(XPDLBrowser.SIMPLE_TYPE_XPDL);
      try {
         WMEntity[] ents = SharkInterfaceWrapper.getShark()
            .getXPDLBrowser()
            .listEntities(null, act, filter, true)
            .getArray();
         for (int i = 0; i < ents.length; i++) {
            WMEntity ent = ents[i];
            if (ent.getType().equals("SubFlow") || ent.getType().equals("BlockActivity")) {
               drawRect = true;
               break;
            }
         }
      } catch (Exception e) {
      }

      int symbolWidth = Integer.parseInt(props.getProperty(ColorManager.GRAPH_SYMBOL_WIDTH));
      int symbolHeight = Integer.parseInt(props.getProperty(ColorManager.GRAPH_SYMBOL_HEIGHT));

      super.setBounds(pos.x + 5, pos.y + 5, symbolWidth - 10, symbolHeight - 10);

      Color color = ColorManager.getColor(props.getProperty(ColorManager.GRAPH_TEXT_COLOR,
                                                            "Color.black"),
                                          Color.black);
      setForeground(color);
      String text = act.getName();
      if (text.equals("")) {
         text = act.getId();
      }
      setText(text);
      setLineWrap(true);
      setWrapStyleWord(true);

      destinationActivities = new ArrayList();

   }

   /**
    * This method adds activity in the list of destination activities
    * 
    * @param activity - activity object that should be added in the list
    */
   public void addDestinationActivity(ActivityJPG activity) {
      destinationActivities.add(activity);
   }

   /**
    * This metod draws activity
    * 
    * @param graphics - Graphics2D object used for drawing
    */
   public void paint(Graphics2D graphics) {
      int symbolWidth = Integer.parseInt(props.getProperty(ColorManager.GRAPH_SYMBOL_WIDTH));
      int symbolHeight = Integer.parseInt(props.getProperty(ColorManager.GRAPH_SYMBOL_HEIGHT));
      int borderWith = 1;
      int arc = 15;

      Color bordercolor = ColorManager.getColor(props.getProperty(ColorManager.GRAPH_ACTIVITY_BORDER_COLOR,
                                                                  "Color.black"),
                                                Color.black);

      Color color = ColorManager.getColor(props.getProperty(name + "_color",
                                                            "Color.orange"), Color.orange);

      int r = color.getRed();
      int gr = color.getGreen();
      int b = color.getBlue();
      Color c1 = new Color(r, gr, b, 255);
      Color c2 = new Color(r, gr, b, 0);
      GradientPaint gp = new GradientPaint(pos.x + borderWith,
                                           pos.y + borderWith,
                                           c1,
                                           symbolWidth - 2 * borderWith,
                                           symbolHeight - 2 * borderWith,
                                           c2);
      ((Graphics2D) graphics).setPaint(gp);
      graphics.fillRoundRect(pos.x + borderWith,
                             pos.y + borderWith,
                             symbolWidth - 2 * borderWith,
                             symbolHeight - 2 * borderWith,
                             arc,
                             arc);

      // border
      graphics.setPaint(bordercolor);
      graphics.drawRoundRect(pos.x, pos.y, symbolWidth, symbolHeight, arc, arc);

      if (drawRect) {
         ((Graphics2D) graphics).setStroke(new BasicStroke(2));
         graphics.setColor(Color.BLACK);
         graphics.drawRoundRect(pos.x+symbolWidth / 2 - 7, pos.y+symbolHeight - 16, 15, 15, 3, 3);
         graphics.drawLine(pos.x+symbolWidth / 2,
                           pos.y+symbolHeight - 11,
                           pos.x+symbolWidth / 2,
                           pos.y+symbolHeight - 5);
         graphics.drawLine(pos.x+symbolWidth / 2 - 3,
                           pos.y+symbolHeight - 8,
                           pos.x+symbolWidth / 2 + 3,
                           pos.y+symbolHeight - 8);
      }

      super.setOpaque(false);
      Graphics2D g2d = (Graphics2D) graphics.create(getLocation().x,
                                                    getLocation().y,
                                                    getSize().width,
                                                    getSize().width);
      super.paint(g2d);
   }

   /**
    * This metod draws transitions
    * 
    * @param graphics - Graphics2D object used for drawing
    */
   public void paintLinks(Graphics2D graphics) {
      Point2D.Double startPoint = new Point2D.Double();
      startPoint.x = pos.x
                     + Double.parseDouble(props.getProperty(ColorManager.GRAPH_SYMBOL_WIDTH));
      startPoint.y = pos.y
                     + Double.parseDouble(props.getProperty(ColorManager.GRAPH_SYMBOL_HEIGHT))
                     / 2;

      Point2D.Double endPoint = new Point2D.Double();
      for (int i = 0; i < destinationActivities.size(); i++) {
         ActivityJPG nextSymbol = (ActivityJPG) destinationActivities.get(i);
         endPoint.x = nextSymbol.getPos().x;
         endPoint.y = nextSymbol.getPos().y
                      + Double.parseDouble(props.getProperty(ColorManager.GRAPH_SYMBOL_HEIGHT))
                      / 2;

         Line2D.Double transition = new Line2D.Double(startPoint, endPoint);

         Color color = ColorManager.getColor(props.getProperty(ColorManager.GRAPH_TRANSITION_COLOR,
                                                               "Color.black"),
                                             Color.black);
         graphics.setColor(color);
         BasicStroke stroke = new BasicStroke(1,
                                              BasicStroke.CAP_SQUARE,
                                              BasicStroke.JOIN_MITER);
         graphics.setStroke(stroke);
         graphics.draw(transition);

         paintArrow(startPoint, endPoint, graphics);
      }
   }

   /**
    * This metod draws arrows on the end of the transition
    * 
    * @param startPoint - transition begin
    * @param endPoint - transition end
    * @param graphics - Graphics2D object used for drawing
    */
   public void paintArrow(Point2D.Double startPoint,
                          Point2D.Double endPoint,
                          Graphics2D graphics) {
      double arrowLength = 15;
      double arrowWidth = 0.3;
      double baseX, baseY;
      double lineX, lineY;
      double linePointX, linePointY;
      double lineNormalX, lineNormalY;
      double lineNormalPointX, lineNormalPointY;

      lineX = endPoint.x - startPoint.x;
      lineY = endPoint.y - startPoint.y;

      linePointX = lineX / Math.sqrt((lineX * lineX) + (lineY * lineY));
      linePointY = lineY / Math.sqrt((lineX * lineX) + (lineY * lineY));

      baseX = endPoint.x - (arrowLength * linePointX);
      baseY = endPoint.y - (arrowLength * linePointY);

      lineNormalX = lineY;
      lineNormalY = -lineX;
      lineNormalPointX = lineNormalX
                         / Math.sqrt((lineNormalX * lineNormalX)
                                     + (lineNormalY * lineNormalY)) * arrowWidth;
      lineNormalPointY = lineNormalY
                         / Math.sqrt((lineNormalX * lineNormalX)
                                     + (lineNormalY * lineNormalY)) * arrowWidth;

      int[] x = new int[3];
      int[] y = new int[3];
      x[0] = (int) endPoint.x;
      x[1] = (int) (baseX + (arrowLength * lineNormalPointX));
      x[2] = (int) (baseX - (arrowLength * lineNormalPointX));

      y[0] = (int) endPoint.y;
      y[1] = (int) (baseY + (arrowLength * lineNormalPointY));
      y[2] = (int) (baseY - (arrowLength * lineNormalPointY));

      Polygon poly = new Polygon(x, y, 3);
      graphics.fillPolygon(poly);
   }

   /**
    * @return activities position
    */
   public Point getPos() {
      return pos;
   }
}
