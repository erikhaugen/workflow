/**
 * Copyright (C) 2015 Bystrobank, JSC
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see http://www.gnu.org/licenses
 */package ru.ilb.workflow.toolagent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.enhydra.jxpdl.elements.ExtendedAttribute;
import org.enhydra.jxpdl.elements.ExtendedAttributes;

import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.ApplicationBusy;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotDefined;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotStarted;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.toolagent.AbstractToolAgent;

/**

 */
public class JaxRsToolAgent extends AbstractToolAgent {

    public static final String RESOURCE_NAME_EXT_ATTR_NAME = "ResourceName";
    public static final String INTERFACE_NAME_EXT_ATTR_NAME = "InterfaceName";
    public static final String METHOD_NAME_EXT_ATTR_NAME = "MethodName";

    private String resourceName;
    private String interfaceName;
    private String methodName;

    @Override
    public void invokeApplication(WMSessionHandle shandle,
            long handle,
            WMEntity appInfo,
            WMEntity toolInfo,
            String applicationName,
            String procInstId,
            String assId,
            AppParameter[] parameters,
            Integer appMode)
            throws ApplicationNotStarted,
            ApplicationNotDefined,
            ApplicationBusy,
            ToolAgentGeneralException {

        super.invokeApplication(shandle,
                handle,
                appInfo,
                toolInfo,
                applicationName,
                procInstId,
                assId,
                parameters,
                appMode);

        try {
            status = APP_STATUS_RUNNING;

            ExtendedAttributes eas = readParamsFromExtAttributes((String) parameters[0].the_value);
            ExtendedAttribute ea;
            ea = eas.getFirstExtendedAttributeForName(RESOURCE_NAME_EXT_ATTR_NAME);
            resourceName = ea.getVValue();
            ea = eas.getFirstExtendedAttributeForName(INTERFACE_NAME_EXT_ATTR_NAME);
            interfaceName = ea.getVValue();
            ea = eas.getFirstExtendedAttributeForName(METHOD_NAME_EXT_ATTR_NAME);
            methodName = ea.getVValue();
            String resourceUrl=(String) new javax.naming.InitialContext().lookup(resourceName);

            ClassLoader cl = getClass().getClassLoader();
            Class iface = cl.loadClass(interfaceName);

            Object resource = JAXRSClientFactory.create(resourceUrl, iface);
            // Get parameter types - ignore 1. param, these are ext.attribs
            Class[] parameterTypes;
            Object[] parameterValues = null;
            AppParameter outParam=null;
            //if (parameters != null) {
                int paramsLength = parameters.length;
                if ("OUT".equals(parameters[paramsLength - 1].the_mode)) {
                    outParam=parameters[paramsLength - 1];
                    paramsLength--;
                }
                parameterTypes = new Class[paramsLength - 1];
                parameterValues = new Object[paramsLength - 1];
                for (int i = 1; i < paramsLength; i++) {
                    if(parameters[i].the_class.equals(Long.class)){
                        parameterTypes[i - 1] = parameters[i].the_class=int.class;
                        parameterValues[i - 1] = parameters[i].the_value!=null?((Long)parameters[i].the_value).intValue():null;
                    }else {
                        parameterTypes[i - 1] = parameters[i].the_class;
                        parameterValues[i - 1] = parameters[i].the_value;
                    }
                }
            //}

            Method m = resource.getClass().getMethod(methodName, parameterTypes);

            if(outParam==null){
                m.invoke(resource, parameterValues);
            }else{
                outParam.the_value=m.invoke(resource, parameterValues);
            }
            status = APP_STATUS_FINISHED;

        } catch (ClassNotFoundException cnf) {
            cus.error(shandle, "JavaClassToolAgent - application "
                    + appName + " terminated incorrectly for [process="
                    + procInstId + ",assignment=" + assId
                    + "], can't find class: " + cnf);
            throw new ApplicationNotDefined("Can't find class " + appName, cnf);
        } catch (NoSuchMethodException nsm) {
            cus.error(shandle, "JavaClassToolAgent - application "
                    + appName + " terminated incorrectly for [process="
                    + procInstId + ",assignment=" + assId
                    + "], can't find method " + methodName + " : "
                    + nsm);
            throw new ApplicationNotDefined("Class "
                    + appName + " doesn't have method "
                    + methodName, nsm);
        } catch (NoClassDefFoundError ncdfe) {
            cus.error(shandle, "JavaClassToolAgent - application "
                    + appName + " terminated incorrectly for [process="
                    + procInstId + ",assignment=" + assId
                    + "], can't find class definition: " + ncdfe);
            throw new ApplicationNotDefined("Class " + appName + " can't be executed", ncdfe);
        } catch (InvocationTargetException ex) {
            Throwable extarget=ex.getTargetException();
            status = APP_STATUS_INVALID;
            cus.error(shandle, "JavaClassToolAgent - application "
                    + appName + " terminated incorrectly for [process="
                    + procInstId + ",assignment=" + assId + "]: " + extarget);
            throw new ToolAgentGeneralException(extarget);

        } catch (Throwable ex) {
            status = APP_STATUS_INVALID;
            cus.error(shandle, "JavaClassToolAgent - application "
                    + appName + " terminated incorrectly for [process="
                    + procInstId + ",assignment=" + assId + "]: " + ex);
            throw new ToolAgentGeneralException(ex);
        }
    }

    public String getInfo(WMSessionHandle shandle) throws ToolAgentGeneralException {
        String i = "This tool agent executes Java classes, by calling its static method called \"execute\"."
                + "\nWhen calling this tool agent's invokeApplication() method, the application "
                + "\nname parameter should be the full name of the class that should be executed "
                + "\nby this tool agent (the classes had to be in the class path) "
                + "\n"
                + "\nThis tool agent is able to understand the extended attribute with the following name:"
                + "\n     * AppName - value of this attribute should represent the class name to be executed"
                + "\n"
                + "\n NOTE: Tool agent will read extended attributes only if they are called through"
                + "\n       Default tool agent (not by shark directly) and this is the case when information "
                + "\n       on which tool agent to start for XPDL application definition is not contained in mappings";
        return i;
    }
}
