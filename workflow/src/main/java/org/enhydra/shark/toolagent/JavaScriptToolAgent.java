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

package org.enhydra.shark.toolagent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.enhydra.jawe.shark.business.SharkConstants;
import org.enhydra.jxpdl.XPDLConstants;
import org.enhydra.jxpdl.elements.ExtendedAttribute;
import org.enhydra.jxpdl.elements.ExtendedAttributes;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmodel.WfActivity;
import org.enhydra.shark.api.client.wfservice.SharkConnection;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.ApplicationBusy;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotDefined;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotStarted;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.utilities.appparameter.AppParameterUtilities;
import org.enhydra.shark.utilities.misc.MiscUtilities;
import org.enhydra.shark.utilities.wmentity.WMEntityUtilities;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

/**
 * Tool agent that executes java scripts. Script can be executed as a file that is stored
 * in tool agent repository, or may be contained within the given application name.
 * 
 * @author Sasa Bojanic
 * @version 1.0
 */
public class JavaScriptToolAgent extends AbstractToolAgent {

   public static final long APP_MODE_FILE = 0;

   public static final long APP_MODE_TEXT = 1;

   protected String script;

   public void invokeApplication(WMSessionHandle shandle,
                                 long handle,
                                 WMEntity appInfo,
                                 WMEntity toolInfo,
                                 String applicationName,
                                 String procInstId,
                                 String assId,
                                 AppParameter[] parameters,
                                 Integer appMode) throws ApplicationNotStarted, ApplicationNotDefined, ApplicationBusy, ToolAgentGeneralException {

      super.invokeApplication(shandle, handle, appInfo, toolInfo, applicationName, procInstId, assId, parameters, appMode);

      try {
         status = APP_STATUS_RUNNING;

         if (appName == null || appName.trim().length() == 0) {
            readParamsFromExtAttributes((String) parameters[0].the_value);
         }
         if (script == null) {
            AppParameter ape = AppParameterUtilities.getParameterByName(parameters, SharkConstants.EA_SCRIPT);
            if (ape != null && ape.the_value != null) {
               script = ape.the_value.toString();
            } else {
               if (appMode != null && appMode.intValue() == APP_MODE_FILE) {
                  script = readScriptFromFile(appName);
               } else {
                  script = appName;
               }
            }
         }

         org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.enter();
         Scriptable scope = prepareContext(cx, parameters);
         Reader sr = new StringReader(script);
         cx.evaluateReader(scope, sr, "<script>", 1, null);
         prepareResult(parameters, scope);

         status = APP_STATUS_FINISHED;
      } catch (IOException ioe) {
         cus.error(shandle, "JavaScriptToolAgent - application "
                            + appName + " terminated incorrectly for [process=" + procInstId + ",assignment=" + assId + "], can't find script file: " + ioe);
         throw new ApplicationNotDefined("Can't find script file " + appName, ioe);
      } catch (Throwable ex) {
         if (ex instanceof JavaScriptException && ((JavaScriptException)ex).getValue() instanceof NativeJavaObject) {
             ex = (Throwable)Context.jsToJava(((NativeJavaObject)((JavaScriptException)ex).getValue()), Throwable.class);
         }
         status = APP_STATUS_INVALID;
         cus.error(shandle, "JavaScriptToolAgent - application "
                            + appName + " terminated incorrectly for [process=" + procInstId + ",assignment=" + assId + "]: " + ex);
         throw new ToolAgentGeneralException(ex);
      } finally {
         Context.exit();
      }

   }

   public String getInfo(WMSessionHandle shandle) throws ToolAgentGeneralException {
      String i = "Executes scripts written in Java script syntax."
                 + "\nIf you set application mode to 0 (zero), tool agent will search for a script "
                 + "\nfile given as applicationName parameter (this file has to be in the class path),"
                 + "\nand if it founds it, it will try to execute it. Otherwise, it will consider "
                 + "applicationName parameter to be the script itself, and will try to execute it." + "\n"
                 + "\nThis tool agent is able to understand the extended attributes with the following names:"
                 + "\n     * AppName - value of this attribute should represent the name of script file to "
                 + "\n                 execute (this file has to be in class path)"
                 + "\n     * Script - the value of this represents the script to be executed. I.e. this"
                 + "\n                extended attribute in XPDL can be defined as follows:"
                 + "\n                      <ExtendedAttribute Name=\"Script\" Value=\"c=a-b;\"/>"
                 + "\n           (a, b and c in above text are Formal parameter Ids from XPDL Application definition)" + "\n"
                 + "\n NOTE: Tool agent will read extended attributes only if they are called through"
                 + "\n       Default tool agent (not by shark directly) and this is the case when information "
                 + "\n       on which tool agent to start for XPDL application definition is not contained in mappings";
      return i;
   }

   protected Scriptable prepareContext(org.mozilla.javascript.Context cx, AppParameter[] context) throws Exception {
      Scriptable scope = cx.initStandardObjects(null);

      String actId = MiscUtilities.getAssignmentActivityId(assId);
      Map svs = WMEntityUtilities.getMapFromWMAttributeArray(Shark.getInstance().getAdminMisc().getSystemVariables(shandle, procInstId, actId));
      Map csvs = WMEntityUtilities.getMapFromWMAttributeArray(Shark.getInstance().getAdminMisc().getConfigStringVariables(shandle, procInstId, actId));
      Map xpdls = WMEntityUtilities.getMapFromWMAttributeArray(Shark.getInstance()
         .getAdminMiscExtension()
         .getXPDLExtendedAttributeStringVariables(shandle, procInstId, actId));

      SharkConnection sconn = Shark.getInstance().getSharkConnection();
      sconn.attachToHandle(shandle);
      String activityId = Shark.getInstance().getAdminMisc().getAssignmentActivityId(sconn.getSessionHandle(), procInstId, assId);

      WfActivity wfActivity = sconn.getActivity(procInstId, activityId);

      Map cntxt = wfActivity.process_context();

      Iterator iter = xpdls.entrySet().iterator();
      while (iter.hasNext()) {
         Map.Entry me = (Map.Entry) iter.next();
         String key = me.getKey().toString();
         if (svs.containsKey(key)) {
            cus.warn(shandle, key + " Shark XPDL String variable conflict, it will be overriden by the system variable value");
         } else if (cntxt.containsKey(key)) {
            cus.warn(shandle, key + " Shark XPDL String variable conflict, it will be overriden by the activity context variable value");
         } else if (context != null && AppParameterUtilities.getParameterByName(context, key) != null) {
            cus.warn(shandle, key + " Shark XPDL String variable conflict, it will be overriden by the context variable value");
         } else if (csvs.containsKey(key)) {
            cus.warn(shandle, key + " Shark XPDL String variable conflict, it will be overriden by the config string variable value");
         }
         scope.put(key, scope, me.getValue());
      }
      iter = csvs.entrySet().iterator();
      while (iter.hasNext()) {
         Map.Entry me = (Map.Entry) iter.next();
         String key = me.getKey().toString();
         if (svs.containsKey(key)) {
            cus.warn(shandle, key + " config string variable conflict, it will be overriden by the system variable value");
         } else if (cntxt.containsKey(key)) {
            cus.warn(shandle, key + " config string variable conflict, it will be overriden by the activity context variable value");
         } else if (context != null && AppParameterUtilities.getParameterByName(context, key) != null) {
            cus.warn(shandle, key + " config string variable conflict, it will be overriden by the context variable value");
         }
         scope.put(key, scope, me.getValue());
      }

      iter = cntxt.entrySet().iterator();
      while (iter.hasNext()) {
         Map.Entry me = (Map.Entry) iter.next();
         String key = me.getKey().toString();
         if (svs.containsKey(key)) {
            cus.warn(shandle, key + " activity context variable conflict, it will be overriden by the system variable value");
         } else if (context != null && AppParameterUtilities.getParameterByName(context, key) != null) {
            cus.warn(shandle, key + " activity context variable conflict, it will be overriden by the context variable value");
         }
         scope.put(key, scope, me.getValue());
      }

      if (context != null) {
         // ignore 1. param - it is ext. attribs
         for (int i = 1; i < context.length; i++) {
            String key = context[i].the_formal_name;
            if (SharkConstants.EA_SCRIPT.equals(key)) {
               continue;
            }
            java.lang.Object value = context[i].the_value;
            if (svs.containsKey(key)) {
               cus.warn(shandle, key + " variable conflict, it will be overriden by the system variable value");
            }
            scope.put(key, scope, value);
         }
      }

      iter = svs.entrySet().iterator();
      while (iter.hasNext()) {
         Map.Entry me = (Map.Entry) iter.next();
         scope.put((String) me.getKey(), scope, me.getValue());
      }
      return scope;
   }

   protected void prepareResult(AppParameter[] context, Scriptable scope) {
      if (context != null) {
         for (int i = 0; i < context.length; i++) {
            if (context[i].the_mode.equals(XPDLConstants.FORMAL_PARAMETER_MODE_OUT) || context[i].the_mode.equals(XPDLConstants.FORMAL_PARAMETER_MODE_INOUT)) {
               java.lang.Object val = scope.get(context[i].the_formal_name, scope);
               /*
                * if (context[i].the_value instanceof Boolean) {
                * context[i].the_value=(Boolean)val; } else if (context[i].the_value
                * instanceof String) { context[i].the_value=(String)val; } else if
                * (context[i].the_value instanceof Integer) { context[i].the_value=new
                * Integer((int)Double.parseDouble(val.toString())); } else if
                * (context[i].the_value instanceof Long) { context[i].the_value=new
                * Long((long)Double.parseDouble(val.toString())); } else if
                * (context[i].the_value instanceof Double) {
                * context[i].the_value=(Double)val; } else if (context[i].the_value
                * instanceof Date) {
                * context[i].the_value=java.text.DateFormat.getDateInstance
                * ().parse(val.toString()); } else { context[i].the_value=val; }
                */
               Class cls = context[i].the_class;
               if (Date.class.isAssignableFrom(cls)) {
                  cls = Date.class;
               }
               context[i].the_value = Context.toType(val, cls);

            }
         }
      }
   }

   protected static String readScriptFromFile(String filename) throws IOException {
      String script = null;
      Reader rdr = null;
      InputStream in = null;
      URL url = null;
      ClassLoader cl = JavaScriptToolAgent.class.getClassLoader();
      url = cl.getResource(filename);
      if (url != null) {
         try {
            in = url.openStream();
         } catch (IOException e) {
         }
      }
      if (in != null) {
         rdr = new InputStreamReader(in);
      }

      if (rdr != null) {
         try {
            BufferedReader brdr = new BufferedReader(rdr);
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = brdr.readLine()) != null) {
               sb.append(line);
               sb.append('\n');
            }
            script = sb.toString();
         } finally {
            rdr.close();
         }
      }
      return script;
   }

   protected ExtendedAttributes readParamsFromExtAttributes(String extAttribs) throws Exception {
      ExtendedAttributes eas = super.readParamsFromExtAttributes(extAttribs);
      if (appName == null || appName.trim().length() == 0) {
         ExtendedAttribute ea = eas.getFirstExtendedAttributeForName(SharkConstants.EA_SCRIPT);
         if (ea != null) {
            script = ea.getVValue();
         }
      }

      return eas;
   }

}
