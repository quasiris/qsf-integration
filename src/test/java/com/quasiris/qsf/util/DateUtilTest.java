package com.quasiris.qsf.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

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

    @Test
    public void getDateAsString() throws Exception {
        Date date = DateUtil.getDate("2020-08-06T00:00:00.000+0200");
        String dateString = DateUtil.getDate(date);
        Assert.assertEquals("2020-08-06T00:00:00.000+0200", dateString);
    }
}