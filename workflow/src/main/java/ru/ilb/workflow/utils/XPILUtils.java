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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.AdminMisc;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.utilities.interfacewrapper.SharkInterfaceWrapper;
import org.enhydra.shark.utilities.misc.MiscUtilities;
import org.enhydra.shark.utilities.wmentity.WMEntityUtilities;
import org.enhydra.shark.webclient.spec.SharkParamConsts;
import org.enhydra.shark.xpil.XPDLDataFieldDocument.DataField;
import org.enhydra.shark.xpil.XPDLExtendedAttributeDocument.ExtendedAttribute;
import org.enhydra.shark.xpil.XPDLExtendedAttributesDocument.ExtendedAttributes;
import org.enhydra.shark.xpil.XPILBooleanArrayDataInstanceDocument.BooleanArrayDataInstance;
import org.enhydra.shark.xpil.XPILBooleanDataInstanceDocument.BooleanDataInstance;
import org.enhydra.shark.xpil.XPILByteArrayDataInstanceDocument.ByteArrayDataInstance;
import org.enhydra.shark.xpil.XPILComplexDataInstanceDocument.ComplexDataInstance;
import org.enhydra.shark.xpil.XPILDataInstance;
import org.enhydra.shark.xpil.XPILDataInstancesDocument.DataInstances;
import org.enhydra.shark.xpil.XPILDateArrayDataInstanceDocument.DateArrayDataInstance;
import org.enhydra.shark.xpil.XPILDateDataInstanceDocument.DateDataInstance;
import org.enhydra.shark.xpil.XPILDateTimeArrayDataInstanceDocument.DateTimeArrayDataInstance;
import org.enhydra.shark.xpil.XPILDateTimeDataInstanceDocument.DateTimeDataInstance;
import org.enhydra.shark.xpil.XPILDoubleArrayDataInstanceDocument.DoubleArrayDataInstance;
import org.enhydra.shark.xpil.XPILDoubleDataInstanceDocument.DoubleDataInstance;
import org.enhydra.shark.xpil.XPILExtendedWorkflowFacilityInstanceDocument;
import org.enhydra.shark.xpil.XPILExtendedWorkflowFacilityInstanceDocument.ExtendedWorkflowFacilityInstance;
import org.enhydra.shark.xpil.XPILInstanceExtendedAttributeDocument.InstanceExtendedAttribute;
import org.enhydra.shark.xpil.XPILInstanceExtendedAttributesDocument.InstanceExtendedAttributes;
import org.enhydra.shark.xpil.XPILLongArrayDataInstanceDocument.LongArrayDataInstance;
import org.enhydra.shark.xpil.XPILLongDataInstanceDocument.LongDataInstance;
import org.enhydra.shark.xpil.XPILMainWorkflowProcessInstanceDocument;
import org.enhydra.shark.xpil.XPILMainWorkflowProcessInstanceDocument.MainWorkflowProcessInstance;
import org.enhydra.shark.xpil.XPILManualActivityInstanceDocument;
import org.enhydra.shark.xpil.XPILSchemaDataInstanceDocument.SchemaDataInstance;
import org.enhydra.shark.xpil.XPILStringArrayDataInstanceDocument.StringArrayDataInstance;
import org.enhydra.shark.xpil.XPILStringDataInstanceDocument.StringDataInstance;
import org.enhydra.shark.xpil.XPILStringValueDocument.StringValue;
import org.enhydra.shark.xpil.XPILTimeArrayDataInstanceDocument.TimeArrayDataInstance;
import org.enhydra.shark.xpil.XPILTimeDataInstanceDocument.TimeDataInstance;

public class XPILUtils {

    public static void filterActivityVariables(WMSessionHandle shandle, XPILExtendedWorkflowFacilityInstanceDocument ins,
            boolean isDynamicVariableHandling,
            boolean ignoreReadOnlyVars) throws Exception {
        MainWorkflowProcessInstance[] mwpiList = ins.getExtendedWorkflowFacilityInstance().getMainWorkflowProcessInstanceArray();
        if (mwpiList != null) {
            for (MainWorkflowProcessInstance mwpi : mwpiList) {
                XPILUtils.filterActivityVariables(shandle, mwpi,
                        isDynamicVariableHandling,
                        ignoreReadOnlyVars);
            }
        }

    }

    public static void filterActivityVariables(WMSessionHandle shandle, MainWorkflowProcessInstance ins,
            boolean isDynamicVariableHandling,
            boolean ignoreReadOnlyVars) throws Exception {
        AdminMisc am = SharkInterfaceWrapper.getShark().getAdminMisc();
        XPILManualActivityInstanceDocument.ManualActivityInstance[] maiList = ins.getActivityInstances().getManualActivityInstanceArray();
        for (XPILManualActivityInstanceDocument.ManualActivityInstance mai : maiList) {

            WMEntity actEnt = am.getActivityDefinitionInfo(shandle, ins.getId(), mai.getId());

            String[][] extAttribs = WMEntityUtilities.getExtAttribNVPairs(shandle,
                    SharkInterfaceWrapper.getShark()
                    .getXPDLBrowser(),
                    actEnt);

            filterActivityVariables(mai.getDataInstances(), extAttribs, isDynamicVariableHandling, ignoreReadOnlyVars);
        }
    }

    public static void filterActivityVariables(DataInstances ins,
            String[][] extAttribs,
            boolean isDynamicVariableHandling,
            boolean ignoreReadOnlyVars)
            throws Exception {

        BooleanDataInstance[] bdisa = ins.getBooleanDataInstanceArray();
        BooleanArrayDataInstance[] badisa = ins.getBooleanArrayDataInstanceArray();
        ComplexDataInstance[] cdisa = ins.getComplexDataInstanceArray();
        DateDataInstance[] dtdisa = ins.getDateDataInstanceArray();
        DateArrayDataInstance[] dtadisa = ins.getDateArrayDataInstanceArray();
        DateTimeDataInstance[] dttdisa = ins.getDateTimeDataInstanceArray();
        DateTimeArrayDataInstance[] dttadisa = ins.getDateTimeArrayDataInstanceArray();
        TimeDataInstance[] ttdisa = ins.getTimeDataInstanceArray();
        TimeArrayDataInstance[] ttadisa = ins.getTimeArrayDataInstanceArray();
        DoubleDataInstance[] dbdisa = ins.getDoubleDataInstanceArray();
        DoubleArrayDataInstance[] dbadisa = ins.getDoubleArrayDataInstanceArray();
        LongDataInstance[] ldisa = ins.getLongDataInstanceArray();
        LongArrayDataInstance[] ladisa = ins.getLongArrayDataInstanceArray();
        SchemaDataInstance[] shdisa = ins.getSchemaDataInstanceArray();
        StringDataInstance[] strdisa = ins.getStringDataInstanceArray();
        StringArrayDataInstance[] stradisa = ins.getStringArrayDataInstanceArray();
        ByteArrayDataInstance[] byadisa = ins.getByteArrayDataInstanceArray();

        String[] varsToDisp = null;
        List varIds = null;
        String[] varNames = null;
        String[] varMandatories = null;
        String[] varMaxLengths = null;

        Map xpilDis = new HashMap();
        if (bdisa != null) {
            for (int i = bdisa.length - 1; i >= 0; i--) {
                xpilDis.put(bdisa[i].getId(), bdisa[i].copy());
                ins.removeBooleanDataInstance(i);
            }
        }
        if (badisa != null) {
            for (int i = badisa.length - 1; i >= 0; i--) {
                xpilDis.put(badisa[i].getId(), badisa[i].copy());
                ins.removeBooleanArrayDataInstance(i);
            }
        }
        if (cdisa != null) {
            for (int i = cdisa.length - 1; i >= 0; i--) {
                xpilDis.put(cdisa[i].getId(), cdisa[i].copy());
                ins.removeComplexDataInstance(i);
            }
        }
        if (dtdisa != null) {
            for (int i = dtdisa.length - 1; i >= 0; i--) {
                xpilDis.put(dtdisa[i].getId(), dtdisa[i].copy());
                ins.removeDateDataInstance(i);
            }
        }
        if (dtadisa != null) {
            for (int i = dtadisa.length - 1; i >= 0; i--) {
                xpilDis.put(dtadisa[i].getId(), dtadisa[i].copy());
                ins.removeDateArrayDataInstance(i);
            }
        }
        if (dttdisa != null) {
            for (int i = dttdisa.length - 1; i >= 0; i--) {
                xpilDis.put(dttdisa[i].getId(), dttdisa[i].copy());
                ins.removeDateTimeDataInstance(i);
            }
        }
        if (dttadisa != null) {
            for (int i = dttadisa.length - 1; i >= 0; i--) {
                xpilDis.put(dttadisa[i].getId(), dttadisa[i].copy());
                ins.removeDateTimeArrayDataInstance(i);
            }
        }
        if (ttdisa != null) {
            for (int i = ttdisa.length - 1; i >= 0; i--) {
                xpilDis.put(ttdisa[i].getId(), ttdisa[i].copy());
                ins.removeTimeDataInstance(i);
            }
        }
        if (ttadisa != null) {
            for (int i = ttadisa.length - 1; i >= 0; i--) {
                xpilDis.put(ttadisa[i].getId(), ttadisa[i].copy());
                ins.removeTimeArrayDataInstance(i);
            }
        }
        if (dbdisa != null) {
            for (int i = dbdisa.length - 1; i >= 0; i--) {
                xpilDis.put(dbdisa[i].getId(), dbdisa[i].copy());
                ins.removeDoubleDataInstance(i);
            }
        }
        if (dbadisa != null) {
            for (int i = dbadisa.length - 1; i >= 0; i--) {
                xpilDis.put(dbadisa[i].getId(), dbadisa[i].copy());
                ins.removeDoubleArrayDataInstance(i);
            }
        }
        if (ldisa != null) {
            for (int i = ldisa.length - 1; i >= 0; i--) {
                xpilDis.put(ldisa[i].getId(), ldisa[i].copy());
                ins.removeLongDataInstance(i);
            }
        }
        if (ladisa != null) {
            for (int i = ladisa.length - 1; i >= 0; i--) {
                xpilDis.put(ladisa[i].getId(), ladisa[i].copy());
                ins.removeLongArrayDataInstance(i);
            }
        }
        if (shdisa != null) {
            for (int i = shdisa.length - 1; i >= 0; i--) {
                xpilDis.put(shdisa[i].getId(), shdisa[i].copy());
                ins.removeSchemaDataInstance(i);
            }
        }
        if (strdisa != null) {
            for (int i = strdisa.length - 1; i >= 0; i--) {
                xpilDis.put(strdisa[i].getId(), strdisa[i].copy());
                if (isDynamicVariableHandling) {
                    if (strdisa[i].getId()
                            .equals(SharkParamConsts.DYNAMIC_VARIABLE_DISPLAY_VARIABLE)) {
                        String dispVarStr = strdisa[i].getValue();
                        if (dispVarStr != null) {
                            varsToDisp = MiscUtilities.tokenize(dispVarStr, ",");
                        }
                    }
                }
                ins.removeStringDataInstance(i);
            }
        }
        if (stradisa != null) {
            for (int i = stradisa.length - 1; i >= 0; i--) {
                xpilDis.put(stradisa[i].getId(), stradisa[i].copy());
                if (isDynamicVariableHandling) {
                    String[] sa = null;
                    if (stradisa[i].getId()
                            .equals(SharkParamConsts.DYNAMIC_VARIABLE_IDS_VARIABLE)
                            || stradisa[i].getId()
                            .equals(SharkParamConsts.DYNAMIC_VARIABLE_NAMES_VARIABLE)
                            || stradisa[i].getId()
                            .equals(SharkParamConsts.DYNAMIC_VARIABLE_MANDATORIES_VARIABLE)
                            || stradisa[i].getId()
                            .equals(SharkParamConsts.DYNAMIC_VARIABLE_MAX_LENGTHS_VARIABLE)) {
                        StringValue[] sva = stradisa[i].getStringValue1Array();
                        if (sva != null) {
                            sa = new String[sva.length];
                            for (int j = 0; j < sva.length; j++) {
                                sa[j] = sva[j].getValue();
                            }
                        }
                        if (stradisa[i].getId()
                                .equals(SharkParamConsts.DYNAMIC_VARIABLE_IDS_VARIABLE)) {
                            varIds = Arrays.asList(sa);
                        } else if (stradisa[i].getId()
                                .equals(SharkParamConsts.DYNAMIC_VARIABLE_NAMES_VARIABLE)) {
                            varNames = sa;
                        } else if (stradisa[i].getId()
                                .equals(SharkParamConsts.DYNAMIC_VARIABLE_MANDATORIES_VARIABLE)) {
                            varMandatories = sa;
                        } else if (stradisa[i].getId()
                                .equals(SharkParamConsts.DYNAMIC_VARIABLE_MAX_LENGTHS_VARIABLE)) {
                            varMaxLengths = sa;
                        }

                    }
                }
                ins.removeStringArrayDataInstance(i);
            }
        }
        if (byadisa != null) {
            for (int i = byadisa.length - 1; i >= 0; i--) {
                xpilDis.put(byadisa[i].getId(), byadisa[i].copy());
                ins.removeByteArrayDataInstance(i);
            }
        }

        if (!isDynamicVariableHandling) {
            for (int i = 0; i < extAttribs.length; i++) {
                String eaName = extAttribs[i][0];

                if (eaName.equals(SharkParamConsts.EA_VAR_TO_PROCESS_UPDATE)
                        || (!ignoreReadOnlyVars && eaName.equals(SharkParamConsts.EA_VAR_TO_PROCESS_VIEW))
                        || eaName.equals(SharkParamConsts.EA_VAR_TO_PROCESS_FETCH)) {
                    String variableId = extAttribs[i][1];
                    String fetchVarId = null;
                    String fetchVarKeys = null;
                    int indOfSC = variableId.indexOf(";");
                    if (indOfSC > 0) {
                        fetchVarId = variableId.substring(indOfSC + 1);
                        variableId = variableId.substring(0, indOfSC);
                        int indOfFK = fetchVarId.indexOf(";");
                        if(indOfFK>0){
                            fetchVarKeys=fetchVarId.substring(indOfFK + 1);
                            fetchVarId=fetchVarId.substring(0,indOfFK);
                        }
                    }
                    if (xpilDis.containsKey(variableId)) {
                        XPILDataInstance di = (XPILDataInstance) xpilDis.get(variableId);
                        InstanceExtendedAttributes ieas = di.getInstanceExtendedAttributes();
                        if (ieas == null) {
                            ieas = di.addNewInstanceExtendedAttributes();
                        }
                        InstanceExtendedAttribute iea = ieas.addNewInstanceExtendedAttribute();
                        iea.setName("how_to_handle_marker");

                        if (eaName.equals(SharkParamConsts.EA_VAR_TO_PROCESS_UPDATE)) {
                            iea.setValue("1");
                        } else if (eaName.equals(SharkParamConsts.EA_VAR_TO_PROCESS_VIEW)) {
                            iea.setValue("0");
                        } else if (eaName.equals(SharkParamConsts.EA_VAR_TO_PROCESS_FETCH)) {
                            if (xpilDis.containsKey(fetchVarId)) {
                                iea.setValue("2");
                                iea = ieas.addNewInstanceExtendedAttribute();
                                iea.setName("fetch_marker");
                                iea.setValue(fetchVarId);
                                XPILDataInstance fetchdi = (XPILDataInstance) xpilDis.get(fetchVarId);
                                InstanceExtendedAttributes fetchieas = fetchdi.getInstanceExtendedAttributes();
                                if (fetchieas == null) {
                                    fetchieas = fetchdi.addNewInstanceExtendedAttributes();
                                }
                                InstanceExtendedAttribute fetchiea = fetchieas.addNewInstanceExtendedAttribute();
                                fetchiea.setName("how_to_handle_marker");
                                fetchiea.setValue("-1");
                                fillInXPILDataInstance(ins, fetchdi);
                            }
                            if(fetchVarKeys!=null && xpilDis.containsKey(fetchVarKeys)){
                                iea = ieas.addNewInstanceExtendedAttribute();
                                iea.setName("fetch_marker_keys");
                                iea.setValue(fetchVarKeys);
                                XPILDataInstance fetchdi = (XPILDataInstance) xpilDis.get(fetchVarKeys);
                                InstanceExtendedAttributes fetchieas = fetchdi.getInstanceExtendedAttributes();
                                if (fetchieas == null) {
                                    fetchieas = fetchdi.addNewInstanceExtendedAttributes();
                                }
                                InstanceExtendedAttribute fetchiea = fetchieas.addNewInstanceExtendedAttribute();
                                fetchiea.setName("how_to_handle_marker");
                                fetchiea.setValue("-1");
                                fillInXPILDataInstance(ins, fetchdi);
                            }
                        }
                        fillInXPILDataInstance(ins, di);
                    }

                } else if (eaName.equals("ENABLE_COMMENTS") && extAttribs[i][1].equalsIgnoreCase("true")) {
                    XPILDataInstance commentDi = (XPILDataInstance) xpilDis.get("comments");
                    InstanceExtendedAttributes fetchieas = commentDi.getInstanceExtendedAttributes();
                    if (fetchieas == null) {
                        fetchieas = commentDi.addNewInstanceExtendedAttributes();
                    }
                    InstanceExtendedAttribute fetchiea = fetchieas.addNewInstanceExtendedAttribute();
                    fetchiea.setName("how_to_handle_marker");
                    fetchiea.setValue("-1");
                    fillInXPILDataInstance(ins, commentDi);
                }
            }
        } else {
            if (varsToDisp != null) {
                for (int i = 0; i < varsToDisp.length; i++) {
                    String variableId = varsToDisp[i];
                    XPILDataInstance di = (XPILDataInstance) xpilDis.get(variableId);
                    if (varIds != null) {
                        int indOf = varIds.indexOf(variableId);
                        if (indOf >= 0) {
                            if (varNames[indOf] != null) {
                                DataField df = di.addNewDataField();
                                df.setId(variableId);
                                df.setName(varNames[indOf]);
                                ExtendedAttributes eas = df.getExtendedAttributes();
                                if (eas == null) {
                                    eas = df.addNewExtendedAttributes();
                                }
                                ExtendedAttribute ea = eas.addNewExtendedAttribute();
                                ea.setName("is_mandatory");
                                if (null != varMandatories
                                        && varMandatories[indOf].equals("false")) {

                                    ea.setValue("false()");

                                } else {
                                    ea.setValue("true()");
                                }
                                ExtendedAttribute ea1 = eas.addNewExtendedAttribute();
                                ea1.setName("max_length");
                                ea1.setValue(null != varMaxLengths ? ("string-length(.) < "
                                        + varMaxLengths[indOf] + "+1")
                                        : "string-length(.) < 255");
                            }
                        }
                    }

                    InstanceExtendedAttributes ieas = di.getInstanceExtendedAttributes();
                    if (ieas == null) {
                        ieas = di.addNewInstanceExtendedAttributes();
                    }
                    InstanceExtendedAttribute iea = ieas.addNewInstanceExtendedAttribute();
                    iea.setName("how_to_handle_marker");
                    iea.setValue("1");
                    fillInXPILDataInstance(ins, di);
                }
            }
        }
    }

    protected static void fillInXPILDataInstance(DataInstances ins,
            XPILDataInstance dins) throws Exception {
        if (dins instanceof BooleanDataInstance) {
            BooleanDataInstance[] dia = ins.getBooleanDataInstanceArray();
            List l = new ArrayList(Arrays.asList(dia));
            l.add(dins);
            dia = new BooleanDataInstance[l.size()];
            l.toArray(dia);
            ins.setBooleanDataInstanceArray(dia);
        } else if (dins instanceof BooleanArrayDataInstance) {
            BooleanArrayDataInstance[] dia = ins.getBooleanArrayDataInstanceArray();
            List l = new ArrayList(Arrays.asList(dia));
            l.add(dins);
            dia = new BooleanArrayDataInstance[l.size()];
            l.toArray(dia);
            ins.setBooleanArrayDataInstanceArray(dia);
        } else if (dins instanceof ComplexDataInstance) {
            ComplexDataInstance[] dia = ins.getComplexDataInstanceArray();
            List l = new ArrayList(Arrays.asList(dia));
            l.add(dins);
            dia = new ComplexDataInstance[l.size()];
            l.toArray(dia);
            ins.setComplexDataInstanceArray(dia);
        } else if (dins instanceof DateDataInstance) {
            DateDataInstance[] dia = ins.getDateDataInstanceArray();
            List l = new ArrayList(Arrays.asList(dia));
            l.add(dins);
            dia = new DateDataInstance[l.size()];
            l.toArray(dia);
            ins.setDateDataInstanceArray(dia);
        } else if (dins instanceof DateArrayDataInstance) {
            DateArrayDataInstance[] dia = ins.getDateArrayDataInstanceArray();
            List l = new ArrayList(Arrays.asList(dia));
            l.add(dins);
            dia = new DateArrayDataInstance[l.size()];
            l.toArray(dia);
            ins.setDateArrayDataInstanceArray(dia);
        } else if (dins instanceof DateTimeDataInstance) {
            DateTimeDataInstance[] dia = ins.getDateTimeDataInstanceArray();
            List l = new ArrayList(Arrays.asList(dia));
            l.add(dins);
            dia = new DateTimeDataInstance[l.size()];
            l.toArray(dia);
            ins.setDateTimeDataInstanceArray(dia);
        } else if (dins instanceof DateTimeArrayDataInstance) {
            DateTimeArrayDataInstance[] dia = ins.getDateTimeArrayDataInstanceArray();
            List l = new ArrayList(Arrays.asList(dia));
            l.add(dins);
            dia = new DateTimeArrayDataInstance[l.size()];
            l.toArray(dia);
            ins.setDateTimeArrayDataInstanceArray(dia);
        } else if (dins instanceof TimeDataInstance) {
            TimeDataInstance[] dia = ins.getTimeDataInstanceArray();
            List l = new ArrayList(Arrays.asList(dia));
            l.add(dins);
            dia = new TimeDataInstance[l.size()];
            l.toArray(dia);
            ins.setTimeDataInstanceArray(dia);
        } else if (dins instanceof TimeArrayDataInstance) {
            TimeArrayDataInstance[] dia = ins.getTimeArrayDataInstanceArray();
            List l = new ArrayList(Arrays.asList(dia));
            l.add(dins);
            dia = new TimeArrayDataInstance[l.size()];
            l.toArray(dia);
            ins.setTimeArrayDataInstanceArray(dia);
        } else if (dins instanceof DoubleDataInstance) {
            DoubleDataInstance[] dia = ins.getDoubleDataInstanceArray();
            List l = new ArrayList(Arrays.asList(dia));
            l.add(dins);
            dia = new DoubleDataInstance[l.size()];
            l.toArray(dia);
            ins.setDoubleDataInstanceArray(dia);
        } else if (dins instanceof DoubleArrayDataInstance) {
            DoubleArrayDataInstance[] dia = ins.getDoubleArrayDataInstanceArray();
            List l = new ArrayList(Arrays.asList(dia));
            l.add(dins);
            dia = new DoubleArrayDataInstance[l.size()];
            l.toArray(dia);
            ins.setDoubleArrayDataInstanceArray(dia);
        } else if (dins instanceof LongDataInstance) {
            LongDataInstance[] dia = ins.getLongDataInstanceArray();
            List l = new ArrayList(Arrays.asList(dia));
            l.add(dins);
            dia = new LongDataInstance[l.size()];
            l.toArray(dia);
            ins.setLongDataInstanceArray(dia);
        } else if (dins instanceof LongArrayDataInstance) {
            LongArrayDataInstance[] dia = ins.getLongArrayDataInstanceArray();
            List l = new ArrayList(Arrays.asList(dia));
            l.add(dins);
            dia = new LongArrayDataInstance[l.size()];
            l.toArray(dia);
            ins.setLongArrayDataInstanceArray(dia);
        } else if (dins instanceof SchemaDataInstance) {
            SchemaDataInstance[] dia = ins.getSchemaDataInstanceArray();
            List l = new ArrayList(Arrays.asList(dia));
            l.add(dins);
            dia = new SchemaDataInstance[l.size()];
            l.toArray(dia);
            ins.setSchemaDataInstanceArray(dia);
        } else if (dins instanceof StringDataInstance) {
            StringDataInstance[] dia = ins.getStringDataInstanceArray();
            List l = new ArrayList(Arrays.asList(dia));
            l.add(dins);
            dia = new StringDataInstance[l.size()];
            l.toArray(dia);
            ins.setStringDataInstanceArray(dia);
        } else if (dins instanceof StringArrayDataInstance) {
            StringArrayDataInstance[] dia = ins.getStringArrayDataInstanceArray();
            List l = new ArrayList(Arrays.asList(dia));
            l.add(dins);
            dia = new StringArrayDataInstance[l.size()];
            l.toArray(dia);
            ins.setStringArrayDataInstanceArray(dia);
        } else if (dins instanceof ByteArrayDataInstance) {
            ByteArrayDataInstance[] dia = ins.getByteArrayDataInstanceArray();
            List l = new ArrayList(Arrays.asList(dia));
            l.add(dins);
            dia = new ByteArrayDataInstance[l.size()];
            l.toArray(dia);
            ins.setByteArrayDataInstanceArray(dia);
        }

    }


}
