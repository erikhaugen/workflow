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
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.Utils;
import org.enhydra.jawe.components.graph.DefaultGraphEventActivityRenderer;
import org.enhydra.jawe.components.graph.GraphActivityInterface;
import org.enhydra.jxpdl.elements.Activity;
import org.enhydra.shark.webclient.spec.Log;

public class SGraphEventActivityRenderer extends DefaultGraphEventActivityRenderer {

   protected static Map threadToColorMap = new HashMap();

   static {
      new ColorMapCleaner().start();
   }

   public static synchronized void putColorMap(Map colorMap) {
      threadToColorMap.put(Thread.currentThread(),
                           new ColorStruct(colorMap, System.currentTimeMillis()));
   }

   protected static synchronized void removeColorMaps(List keys) {
      Iterator it = keys.iterator();
      while (it.hasNext()) {
         threadToColorMap.remove(it.next());
      }
   }

   public void paint(Graphics g) {
      super.paint(g);
   }

   public Color getFillColor() {
      Color c = null;
      Activity act = (Activity) ((GraphActivityInterface) view.getCell()).getUserObject();
      ColorStruct cs = (ColorStruct) threadToColorMap.get(Thread.currentThread());
      if (cs != null) {
         c = (Color) cs.colorMap.get(act.getId());
      }
      if (c == null) {
         c = super.getFillColor();
      }
      return c;
   }

   public ImageIcon getIcon() {
      Activity act = (Activity) ((GraphActivityInterface) view.getCell()).getUserObject();

      String icon = act.getIcon();

      ImageIcon ii = null;
      if (!icon.equals("")) {
         Map m = Utils.getOriginalActivityIconsMap();
         ii = (ImageIcon) m.get(icon);
         if (ii == null) {
            try {
               ii = new ImageIcon(Utils.class.getClassLoader()
                  .getResource("org/enhydra/jawe/activityicons/" + icon));
            } catch (Exception ex) {

            }
         }
         if (ii != null) {
            m.put(icon, ii);
         }
      }

      if (ii == null) {
         ii = JaWEManager.getInstance()
            .getJaWEController()
            .getTypeResolver()
            .getJaWEType(act)
            .getIcon();
      }

      return ii;
   }

   static class ColorStruct {
      public Map colorMap;

      public long timeStamp;

      ColorStruct(Map m, long ts) {
         this.colorMap = m;
         this.timeStamp = ts;
      }
   }

   static class ColorMapCleaner extends Thread {

      public ColorMapCleaner() {
         this.setDaemon(true);
      }

      public void run() {
         long maxLifeTime = 180000;
         while (true) {
            try {
               Thread.sleep(maxLifeTime);
               Iterator it = threadToColorMap.entrySet().iterator();
               List toRemove = new ArrayList();
               while (it.hasNext()) {
                  Map.Entry me = (Map.Entry) it.next();
                  Object key = me.getKey();
                  ColorStruct cs = (ColorStruct) me.getValue();
                  if (System.currentTimeMillis() - maxLifeTime > cs.timeStamp) {
                     toRemove.add(key);
                  }
               }
               removeColorMaps(toRemove);
            } catch (Throwable ex) {
               Log.logException("Failed to clear color map entries! There are "
                                      + threadToColorMap.size()
                                      + " entries left in the map.",
                                ex);
            }
         }
      }
   }
}
