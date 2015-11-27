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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import org.enhydra.shark.api.client.wfservice.NameValue;
import org.enhydra.shark.api.client.xpil.XPILHandler;

/**
 *
 * @author slavb
 */
public class XpilpropUtils {

        public static void convertXpilpropListToProperties(List<String> xpilprop,Properties props ) {
        if (xpilprop != null && xpilprop.size() > 0) {
            for (String p : xpilprop) {
                String[] parts=p.split("=");
                props.setProperty(toSharkValue(parts[0]), parts.length==1?"true":parts[1]);
            }
        }
    }

    public static NameValue[] convertXpilpropListToNameValueArray(List<String> xpilprop) {
        NameValue[] nvarr = null;
        if (xpilprop != null && xpilprop.size() > 0) {
            nvarr = new NameValue[xpilprop.size()];
            int i = 0;
            for (String p : xpilprop) {
                String[] parts=p.split("=");
                nvarr[i++] = new NameValue(toSharkValue(parts[0]), parts.length==1?"true":parts[1]);
            }
        }
        return nvarr;
    }
    public static String toSharkValue(String p){
        try {
            Field f = XPILHandler.class.getField(p);
            return (String) f.get(null);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

}
