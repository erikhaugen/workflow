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

package org.enhydra.shark.webclient.spec;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.GregorianCalendar;

import com.lutris.appserver.server.Enhydra;
import com.lutris.logging.Logger;

/**
 * Class used for logging messages to file.
 */
public class Log {

   /**
    * When set to 'true' , this class will not log into file.
    */
   private static boolean disabled = false;

   private static String getLogFile() {
      return ('/' == File.separatorChar ? "/tmp" : "") + "/test.log";
   }

   /**
    * Logs message to default file.
    * 
    * @param message message for logging,
    */
   public static void logToFile(String message) {
      Enhydra.getLogChannel().write(Logger.INFO, message);

   }

   public static void log(String message) {

      Enhydra.getLogChannel().write(Logger.INFO, message);
   }

   /**
    * Logs message to file.
    * 
    * @param fileLog file path and name which is used for logging.
    * @param message message for logging,
    */
   public static void logToFile(String fileLog, String message) {
      try {
         if (!disabled) {
            File file = new File(fileLog);
            if (!file.exists()) {
               file.createNewFile();
            }
            RandomAccessFile fileLogr = new RandomAccessFile(fileLog, "rw");
            fileLogr.seek(fileLogr.length());
            fileLogr.writeBytes(getTotalTime() + message + "\r\n");
            fileLogr.close();
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   public static void logException(Throwable e) {
      logException(null, e);
   }

   public static void logException(String msg, Throwable e) {
      if (!disabled) {
         if (Enhydra.getLogChannel() != null) {
            if (e != null) {
               Enhydra.getLogChannel().write(Logger.CRITICAL, msg != null ? msg : e.getMessage(), e);
            } else {
               Enhydra.getLogChannel().write(Logger.CRITICAL, msg);
            }
         } else {
            System.out.println(msg);
         }
      }
   }

   private static String getTotalTime() {
      StringBuffer time = new StringBuffer();
      time.append("[");
      GregorianCalendar cal = new GregorianCalendar();
      time.append(cal.get(GregorianCalendar.HOUR_OF_DAY));
      time.append(":");
      time.append(cal.get(GregorianCalendar.MINUTE));
      time.append(":");
      time.append(cal.get(GregorianCalendar.SECOND));
      time.append("]");
      time.append("  ");
      return time.toString();
   }
}
