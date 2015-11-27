/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.together._2006.xpil1.impl;

import at.together._2006.xpil1.InstanceExtendedAttributes;
import at.together._2006.xpil1.LanguageMappings;
import at.together._2006.xpil1.StringArrayDataInstance;
import at.together._2006.xpil1.StringValue;
import java.util.ArrayList;
import java.util.List;
import org.wfmc._2002.xpdl1.DataField;
import org.wfmc._2002.xpdl1.FormalParameter;

/**
 *
 * @author slavb
 */
public class StringArrayDataInstanceImpl extends StringArrayDataInstance {

    public StringArrayDataInstanceImpl() {
    }

    public StringArrayDataInstanceImpl(FormalParameter formalParameter, DataField dataField, LanguageMappings languageMappings, InstanceExtendedAttributes instanceExtendedAttributes, String id, List<StringValue> stringValues) {
        super(formalParameter, dataField, languageMappings, instanceExtendedAttributes, id, stringValues);
    }

    public List<String> getValue() {
        List<String> list = null;
        if (stringValues != null) {
            list = new ArrayList<>();
            for (StringValue stringValue : stringValues) {
                list.add(stringValue.getValue());
            }
        }
        return list;
    }
}
