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

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;

/**
 *
 * @author slavb
 */
public class Duration {

    public static Date addTo(Date d, String duration) {
        return addTo(d, duration, null, null);
    }
    public static Date addTo(Date d, String duration, String hourmin, String hourmax) {
        Calendar c = new GregorianCalendar();
        c.setTime(d);
        try {
            javax.xml.datatype.DatatypeFactory.newInstance().newDuration(duration).addTo(c);
        } catch (DatatypeConfigurationException ex) {
            throw new RuntimeException(ex);
        }
        Integer hourminInt=hourmin!=null?Integer.parseInt(hourmin):null;
        if (hourminInt != null && c.get(Calendar.HOUR_OF_DAY) < hourminInt) {
            c.set(Calendar.HOUR_OF_DAY, hourminInt);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
        }
        Integer hourmaxInt=hourmax!=null?Integer.parseInt(hourmax):null;
        if (hourmaxInt != null && c.get(Calendar.HOUR_OF_DAY) > hourmaxInt) {
            c.add(Calendar.DATE, 1);
            c.set(Calendar.HOUR_OF_DAY, hourminInt);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);

        }
        return c.getTime();
    }
    public static Timestamp addToTS(Date d, String duration, String hourmin, String hourmax) {
        return new Timestamp(addTo(d, duration, hourmin, hourmax).getTime());
    }
    public static GregorianCalendar addToGC(Date d, String duration, String hourmin, String hourmax) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(addTo(d, duration, hourmin, hourmax));
        return gc;
    }

}
