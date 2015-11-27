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
import java.lang.reflect.Field;

/**
 * ColorManager is util calss that return Color object based on proceeded string 
 * 
 * @author Zorica Dudarin
 */
public class ColorManager {

   public static final String GRAPH_SYMBOL_WIDTH = "graph_symbol_width";
   
   public static final String GRAPH_SYMBOL_HEIGHT = "graph_symbol_height";
   
   public static final String GRAPH_HORIZONTAL_DIST = "graph_horizontal_dist";
   
   public static final String GRAPH_VERTICAL_DIST = "graph_vertical_dist";
   
   public static final String GRAPH_BACKGROUND_COLOR = "graph_background_color";
   
   public static final String GRAPH_TRANSITION_COLOR = "graph_transition_color";
   
   public static final String GRAPH_TEXT_COLOR = "graph_text_color";
   
   public static final String GRAPH_ACTIVITY_FINISHED_COLOR = "graph_activity_finished_color";
   
   public static final String GRAPH_ACTIVITY_PENDING_COLOR = "graph_activity_pending_color";
   
   public static final String GRAPH_ACTIVITY_RUNNING_COLOR = "graph_activity_running_color";

   public static final String GRAPH_ACTIVITY_NONEXECUTED_COLOR = "graph_activity_nonexecuted_color";
   
   public static final String GRAPH_ACTIVITY_BORDER_COLOR = "graph_activity_border_color";
   
   
   //passed string must look like this "R=123,G=33,B=123" or this "Color.red" string
	public static Color getColor(String col,Color defaultColor) {
		Color c = defaultColor;
		int dotInd = col.indexOf(".");
		int r, g, b;
		if (col.indexOf("Color") != -1 && dotInd != -1) {
			try {
				ClassLoader cl = ColorManager.class.getClassLoader();
				Class cls = cl.loadClass("java.awt." + col.substring(0, dotInd));
				Field f = cls.getField(col.substring(dotInd + 1));
				c = (Color) f.get(null);
				c = new Color(c.getRed(), c.getGreen(), c.getBlue());
			} catch (Exception ex) {
			}
		} else {
			try {
				int i1 = col.indexOf("R=");
				if (i1 == -1)
					i1 = col.indexOf("r=");
				int i1c = col.indexOf(",", i1 + 2);
				int i2 = col.indexOf("G=");
				if (i2 == -1)
					i2 = col.indexOf("g=");
				int i2c = col.indexOf(",", i2 + 2);
				int i3 = col.indexOf("B=");
				if (i3 == -1)
					i3 = col.indexOf("b=");
				if (i1 != -1 && i1c != -1 && i2 != -1 && i2c != -1 && i3 != -1 && (i1c < i2) && (i2c < i3)) {
					r = Integer.valueOf(col.substring(i1 + 2, i1c).trim()).intValue();
					if (r < 0)
						r = 0;
					if (r > 255)
						r = 255;
					g = Integer.valueOf(col.substring(i2 + 2, i2c).trim()).intValue();
					if (g < 0)
						g = 0;
					if (g > 255)
						g = 255;
					b = Integer.valueOf(col.substring(i3 + 2).trim()).intValue();
					if (b < 0)
						b = 0;
					if (b > 255)
						b = 255;
					c = new Color(r, g, b);
				}
			} catch (Exception ex) {
			}
		}
		return c;
	}
}
