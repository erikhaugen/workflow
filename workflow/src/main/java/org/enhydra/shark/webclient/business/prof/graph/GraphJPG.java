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
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JPanel;

import org.enhydra.shark.api.client.wfservice.WMEntity;

/**
 * GraphJPG class represents graph that contains one or none finished activity, one
 * pending activity and none or more nonexecuted activities. This graph will be used for
 * creating JPG picture.
 * 
 * @author Zorica Dudarin
 */
public class GraphJPG extends JPanel {

   // default graphic properties
   public static final String GRAPH_SYMBOL_WIDTH_DEFAULT = "90";

   public static final String GRAPH_SYMBOL_HEIGHT_DEFAULT = "60";

   public static final String GRAPH_HORIZONTAL_DIST_DEFAULT = "60";

   public static final String GRAPH_VERTICAL_DIST_DEFAULT = "20";

   public static final String GRAPH_BACKGROUND_COLOR_DEFAULT = "Color.white";

   public static final String GRAPH_TRANSITION_COLOR_DEFAULT = "R=0,G=128,B=255";

   public static final String GRAPH_TEXT_COLOR_DEFAULT = "Color.black";

   public static final String GRAPH_ACTIVITY_FINISHED_COLOR_DEFAULT = "R=255,G=128,B=128";

   public static final String GRAPH_ACTIVITY_PENDING_COLOR_DEFAULT = "R=128,G=255,B=128";

   public static final String GRAPH_ACTIVITY_NONEXECUTED_COLOR_DEFAULT = "R=255,G=255,B=255";

   public static final String GRAPH_ACTIVITY_BORDER_COLOR_DEFAULT = "Color.black";

   private Properties props = null;

   /**
    * Graphs dimensions
    */
   private Dimension dimension = null;

   /**
    * Graphs background
    */
   private Rectangle rect = null;

   /**
    * List of all activities no the graph
    */
   private List activities = null;

   /**
    * Constructor
    * 
    * @param props - object with graph/activities settings
    */
   public GraphJPG(Properties props) {
      this.props = props;

      if (props == null) {
         this.props = new Properties();
      }
      if (this.props.size() == 0) {
         setDefaultProperties();
      }
   }

   /**
    * This method fill properties object if it isn't proceeded or it's empty
    */
   private void setDefaultProperties() {
      props.setProperty(ColorManager.GRAPH_SYMBOL_WIDTH, GRAPH_SYMBOL_WIDTH_DEFAULT);
      props.setProperty(ColorManager.GRAPH_SYMBOL_HEIGHT, GRAPH_SYMBOL_HEIGHT_DEFAULT);
      props.setProperty(ColorManager.GRAPH_HORIZONTAL_DIST, GRAPH_HORIZONTAL_DIST_DEFAULT);
      props.setProperty(ColorManager.GRAPH_VERTICAL_DIST, GRAPH_VERTICAL_DIST_DEFAULT);
      props.setProperty(ColorManager.GRAPH_TRANSITION_COLOR,
                        GRAPH_TRANSITION_COLOR_DEFAULT);
      props.setProperty(ColorManager.GRAPH_BACKGROUND_COLOR,
                        GRAPH_BACKGROUND_COLOR_DEFAULT);
      props.setProperty(ColorManager.GRAPH_TEXT_COLOR, GRAPH_TEXT_COLOR_DEFAULT);
      props.setProperty(ColorManager.GRAPH_ACTIVITY_FINISHED_COLOR,
                        GRAPH_ACTIVITY_FINISHED_COLOR_DEFAULT);
      props.setProperty(ColorManager.GRAPH_ACTIVITY_PENDING_COLOR,
                        GRAPH_ACTIVITY_PENDING_COLOR_DEFAULT);
      props.setProperty(ColorManager.GRAPH_ACTIVITY_NONEXECUTED_COLOR,
                        GRAPH_ACTIVITY_NONEXECUTED_COLOR_DEFAULT);
      props.setProperty(ColorManager.GRAPH_ACTIVITY_BORDER_COLOR,
                        GRAPH_ACTIVITY_BORDER_COLOR_DEFAULT);
   }

   /**
    * This method calculate graphs dimensions based on number of activities.
    * 
    * @param col - number of different kind of activities (finished, pending and
    *           nonexecuted) are placed one next to another (horizontal)
    * @param row - number of nonexecuted activities which are placed one abowe another
    */
   private void calculateDimension(int col, int row) {
      if (props != null) {
         int width = col
                     * Integer.parseInt(props.getProperty(ColorManager.GRAPH_SYMBOL_WIDTH))
                     + 10
                     + (col - 1)
                     * Integer.parseInt(props.getProperty(ColorManager.GRAPH_HORIZONTAL_DIST));
         int height = row
                      * Integer.parseInt(props.getProperty(ColorManager.GRAPH_SYMBOL_HEIGHT))
                      + 10
                      + (row - 1)
                      * Integer.parseInt(props.getProperty(ColorManager.GRAPH_VERTICAL_DIST));
         dimension = new Dimension(width, height);
      }
   }

   /**
    * This method create acivities on cetrain positions on the graph
    * 
    * @param finished - finished activity name
    * @param pending - pending activity name
    * @param nonexecuted - list of nonexecuted activities name
    */
   public void createSymbols(WMEntity finished, WMEntity pending, ArrayList nonexecuted) {
      activities = new ArrayList();

      int col = 3;
      int row = (nonexecuted == null || nonexecuted.size() == 0) ? 1 : nonexecuted.size();
      calculateDimension(col, row);
      rect = new Rectangle(dimension);

      int colTemp = 1;
      colTemp = finished == null ? colTemp : colTemp + 1;
      colTemp = (nonexecuted == null || nonexecuted.size() < 1) ? colTemp : colTemp + 1;

      int x = 0;
      int y = 0;
      Point pos = null;

      // finished
      x = dimension.width
          / 2
          - (colTemp
             * Integer.parseInt(props.getProperty(ColorManager.GRAPH_SYMBOL_WIDTH)) + (colTemp - 1)
                                                                                      * Integer.parseInt(props.getProperty(ColorManager.GRAPH_HORIZONTAL_DIST)))
          / 2;
      y = dimension.height
          / 2 - Integer.parseInt((props.getProperty(ColorManager.GRAPH_SYMBOL_HEIGHT)))
          / 2;
      pos = new Point(x, y);
      ActivityJPG previos = null;
      if (finished != null) {
         previos = new ActivityJPG(props, "graph_activity_finished", finished, pos);
         activities.add(previos);

         x += Integer.parseInt(props.getProperty(ColorManager.GRAPH_SYMBOL_WIDTH))
              + Integer.parseInt(props.getProperty(ColorManager.GRAPH_HORIZONTAL_DIST));
      }

      // pending
      pos = new Point(x, y);
      ActivityJPG current = null;
      if (pending != null) {
         current = new ActivityJPG(props, "graph_activity_pending", pending, pos);
         activities.add(current);

         x += Integer.parseInt(props.getProperty(ColorManager.GRAPH_SYMBOL_WIDTH))
              + Integer.parseInt(props.getProperty(ColorManager.GRAPH_HORIZONTAL_DIST));
      }

      // nonexecuted
      y = 5;
      pos = new Point(x, y);
      ArrayList nexts = new ArrayList();
      if (nonexecuted != null) {
         for (int i = 0; i < nonexecuted.size(); i++) {
            ActivityJPG next = new ActivityJPG(props,
                                               "graph_activity_nonexecuted",
                                               (WMEntity) nonexecuted.get(i),
                                               pos);
            nexts.add(next);
            activities.add(next);

            y += Integer.parseInt(props.getProperty(ColorManager.GRAPH_SYMBOL_HEIGHT))
                 + Integer.parseInt(props.getProperty(ColorManager.GRAPH_VERTICAL_DIST));
            pos = new Point(x, y);
         }
      }

      // transitions
      if (previos != null) {
         previos.addDestinationActivity(current);
      }
      for (int i = 0; i < nexts.size(); i++) {
         current.addDestinationActivity((ActivityJPG) nexts.get(i));
      }
   }

   /**
    * This metod draw grath passing thru list of activities and calling its paint method.
    * On the end it draws transitions between them
    * 
    * @param graphics - Graphics2D object used for drawing
    * @throws Exception
    */
   public void paint(Graphics2D graphics) {
      Color color = ColorManager.getColor(props.getProperty(ColorManager.GRAPH_BACKGROUND_COLOR,
                                                            "Color.white"),
                                          Color.white);
      graphics.setPaint(color);
      graphics.fill(rect);

      for (int i = 0; i < activities.size(); i++) {
         ((ActivityJPG) activities.get(i)).paint(graphics);
      }

      for (int i = 0; i < activities.size(); i++) {
         ((ActivityJPG) activities.get(i)).paintLinks(graphics);
      }
   }

   /**
    * @return graphs dimension
    */
   public Dimension getDimension() {
      return dimension;
   }
}
