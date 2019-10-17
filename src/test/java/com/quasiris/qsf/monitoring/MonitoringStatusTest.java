package com.quasiris.qsf.monitoring;

import org.junit.Assert;
import org.junit.Test;

public class MonitoringStatusTest {


    @Test
    public void computeStatusCurrentERROR() {
        String actual = MonitoringStatus.computeStatus(MonitoringStatus.ERROR, MonitoringStatus.OK);
        Assert.assertEquals(MonitoringStatus.ERROR, actual);
        actual = MonitoringStatus.computeStatus(MonitoringStatus.ERROR, MonitoringStatus.WARN);
        Assert.assertEquals(MonitoringStatus.ERROR, actual);
        actual = MonitoringStatus.computeStatus(MonitoringStatus.ERROR, MonitoringStatus.ERROR);
        Assert.assertEquals(MonitoringStatus.ERROR, actual);
    }


    @Test
    public void computeStatusCurrentWARN() {
        String actual = MonitoringStatus.computeStatus(MonitoringStatus.WARN, MonitoringStatus.OK);
        Assert.assertEquals(MonitoringStatus.WARN, actual);
        actual = MonitoringStatus.computeStatus(MonitoringStatus.WARN, MonitoringStatus.WARN);
        Assert.assertEquals(MonitoringStatus.WARN, actual);
        actual = MonitoringStatus.computeStatus(MonitoringStatus.WARN, MonitoringStatus.ERROR);
        Assert.assertEquals(MonitoringStatus.ERROR, actual);
    }

    @Test
    public void computeStatusCurrentOK() {
        String actual = MonitoringStatus.computeStatus(MonitoringStatus.OK, MonitoringStatus.OK);
        Assert.assertEquals(MonitoringStatus.OK, actual);
        actual = MonitoringStatus.computeStatus(MonitoringStatus.OK, MonitoringStatus.WARN);
        Assert.assertEquals(MonitoringStatus.WARN, actual);
        actual = MonitoringStatus.computeStatus(MonitoringStatus.OK, MonitoringStatus.ERROR);
        Assert.assertEquals(MonitoringStatus.ERROR, actual);
    }

    @Test
    public void computeStatusNewNull() {
        String actual = MonitoringStatus.computeStatus(MonitoringStatus.ERROR, null);
        Assert.assertEquals(MonitoringStatus.ERROR, actual);
        actual = MonitoringStatus.computeStatus(MonitoringStatus.WARN, null);
        Assert.assertEquals(MonitoringStatus.WARN, actual);
        actual = MonitoringStatus.computeStatus(MonitoringStatus.OK, null);
        Assert.assertEquals(MonitoringStatus.OK, actual);
    }

    @Test
    public void computeStatusCurrentNULL() {
        String actual = MonitoringStatus.computeStatus(null, MonitoringStatus.OK);
        Assert.assertEquals(MonitoringStatus.OK, actual);
        actual = MonitoringStatus.computeStatus(null, MonitoringStatus.WARN);
        Assert.assertEquals(MonitoringStatus.WARN, actual);
        actual = MonitoringStatus.computeStatus(null, MonitoringStatus.ERROR);
        Assert.assertEquals(MonitoringStatus.ERROR, actual);
    }

    @Test
    public void computeStatusCurrentAndNewNULL() {
        String actual = MonitoringStatus.computeStatus(null, null);
        Assert.assertNull(actual);
    }
}