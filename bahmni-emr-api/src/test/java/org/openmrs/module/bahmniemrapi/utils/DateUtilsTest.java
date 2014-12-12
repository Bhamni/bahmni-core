package org.openmrs.module.bahmniemrapi.utils;


import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class DateUtilsTest {

    @Test
    public void shouldReturnEODTimeGivenADate() {

        Date date = new DateTime(2014, 12, 11, 9, 0).toDate();
        Date expectedEOD = new DateTime(2014, 12, 11, 23, 59, 59).toDate();

        Date eod = DateUtils.eod(date);

        assertEquals(expectedEOD, eod);
    }

    @Test
    public void shouldReturnSODTimeGivenADate() {

        Date date = new DateTime(2014, 12, 11, 9, 0).toDate();
        Date sod = new DateTime(2014, 12, 11, 0, 0, 0).toDate();

        Date actualSod = DateUtils.sod(date);

        assertEquals(sod, actualSod);
    }

    @Test
    public void shouldReturnFalseForSameDateDifferentTimeStamp() throws Exception {
        Date date1 = new DateTime(2014, 12, 11, 9, 0).toDate();
        Date date2 = new DateTime(2014, 12, 11, 23, 59, 59).toDate();
        assertFalse(DateUtils.isAfter(date1, date2));
    }

    @Test
    public void shouldReturnFalseForBeforeDateDifferentTimeStamp() throws Exception {
        Date date1 = new DateTime(2014, 12, 10, 9, 0).toDate();
        Date date2 = new DateTime(2014, 12, 11, 23, 59, 59).toDate();
        assertFalse(DateUtils.isAfter(date1, date2));
    }


    @Test
    public void shouldReturnTrueForAfterDate() throws Exception {
        Date date1 = new DateTime(2014, 12, 13, 9, 0).toDate();
        Date date2 = new DateTime(2014, 12, 11, 23, 59, 59).toDate();
        assertTrue(DateUtils.isAfter(date1, date2));
    }
}