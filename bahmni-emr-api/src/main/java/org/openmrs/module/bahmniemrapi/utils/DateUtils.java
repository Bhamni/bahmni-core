package org.openmrs.module.bahmniemrapi.utils;

import org.joda.time.DateTime;

import java.util.Date;

public class DateUtils {

    public static Date eod(Date date) {
        return new DateTime(date).toDateMidnight().toDateTime().plusDays(1).minusSeconds(1).toDate();
    }

    public static Date sod(Date date) {
        return new DateTime(date).toDateMidnight().toDate();
    }

    public static Boolean isAfter(Date date1, Date date2){
        return new DateTime(date1).toDateMidnight().isAfter(new DateTime(date2).toDateMidnight());
    }

}
