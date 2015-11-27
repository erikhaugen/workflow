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
 */
package ru.ilb.workflow.utils;

import at.together._2006.xpil1.DataInstance;
import at.together._2006.xpil1.ExecutionInstance;
import at.together._2006.xpil1.ExtendedWorkflowFacilityInstance;
import at.together._2006.xpil1.MainWorkflowProcessInstance;
import at.together._2006.xpil1.PackageInstances;
import at.together._2006.xpil1.SubWorkflowProcessInstance;
import at.together._2006.xpil1.WorkflowProcessInstance;
import at.together._2006.xpil1.impl.WorkflowProcessInstanceImpl;
import at.together._2006.xpil1.WorkflowProcessInstances;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author slavb
 */
public class XPILJAXBUtils {

    private static String fixXml(String s) {
        return s.replace("LongDataInstance Value=\"\"", "LongDataInstance");
    }

    public static ExtendedWorkflowFacilityInstance fromString(String s) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance("at.together._2006.xpil1");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            ExtendedWorkflowFacilityInstance res = (ExtendedWorkflowFacilityInstance) unmarshaller.unmarshal(new StringReader(fixXml(s)));
            return res;
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }

    }

    public static WorkflowProcessInstance fromStringWfProcess(String s) {
        try {
            s = s.replace("LongDataInstance Value=\"\"", "LongDataInstance");
            XMLInputFactory xif = XMLInputFactory.newFactory();
            XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(fixXml(s)));
            JAXBContext jaxbContext = JAXBContext.newInstance("at.together._2006.xpil1");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            WorkflowProcessInstance res = null;

            while (xsr.hasNext()) {
                if (xsr.isStartElement() && (xsr.getLocalName().equals("MainWorkflowProcessInstance") || xsr.getLocalName().equals("SubWorkflowProcessInstance"))) {
                    switch (xsr.getLocalName()) {
                        case "MainWorkflowProcessInstance":
                            res = unmarshaller.unmarshal(xsr, MainWorkflowProcessInstance.class).getValue();
                            break;
                        case "SubWorkflowProcessInstance":
                            res = unmarshaller.unmarshal(xsr, SubWorkflowProcessInstance.class).getValue();
                            break;
                    }
                    break;
                }
                xsr.next();
            }
            return res;
        } catch (XMLStreamException | JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static PackageInstances fromStringPackageInstances(String s) {
        try {
            XMLInputFactory xif = XMLInputFactory.newFactory();
            XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(fixXml(s)));
            while (xsr.hasNext()) {
                if (xsr.isStartElement() && xsr.getLocalName().equals("PackageInstances")) {
                    break;
                }
                xsr.next();
            }
            JAXBContext jaxbContext = JAXBContext.newInstance("at.together._2006.xpil1");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            PackageInstances res = unmarshaller.unmarshal(xsr, PackageInstances.class).getValue();
            return res;
        } catch (XMLStreamException | JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static WorkflowProcessInstances fromStringWfProcesses(String str) {
        ExtendedWorkflowFacilityInstance inst = fromString(str);
        WorkflowProcessInstances result = new WorkflowProcessInstances();
        for (Serializable s : inst.getUsersAndUsersAndPackageInstances()) {
            if (s instanceof WorkflowProcessInstanceImpl) {
                result.getMainWorkflowProcessInstancesAndSubWorkflowProcessInstances().add((WorkflowProcessInstanceImpl) s);
            }
        }
        return result;

    }

    public static String toString(ExtendedWorkflowFacilityInstance extendedworkflowfacilityinstance) {
        try {
            StringWriter writer = new StringWriter();
            JAXBContext jaxbContext = JAXBContext.newInstance("at.together._2006.xpil1");
            Marshaller m = jaxbContext.createMarshaller();
            m.marshal(extendedworkflowfacilityinstance, writer);
            return writer.toString();
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }
}
