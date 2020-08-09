package com.quasiris.qsf.util;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

public class DateUtilTest {

    @Test
    public void getDateDifferentTimezoneFormat() throws Exception {
        Date date1 = DateUtil.getDate("2020-08-06T22:18:26.528+0000");
        Date date2 = DateUtil.getDate("2020-08-06T22:18:26.528+00:00");
        Assert.assertEquals(date1, date2);
    }

    @Test
    public void getDateWithoutTime() throws Exception {
        Date date = DateUtil.getDate("2020-08-06");
        Assert.assertNotNull(date);
    }
}