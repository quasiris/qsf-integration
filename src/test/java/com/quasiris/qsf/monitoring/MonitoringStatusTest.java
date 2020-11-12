package com.quasiris.qsf.monitoring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MonitoringStatusTest {


    @Test
    public void computeStatusCurrentERROR() {
        String actual = MonitoringStatus.computeStatus(MonitoringStatus.ERROR, MonitoringStatus.OK);
        assertEquals(MonitoringStatus.ERROR, actual);
        actual = MonitoringStatus.computeStatus(MonitoringStatus.ERROR, MonitoringStatus.WARN);
        assertEquals(MonitoringStatus.ERROR, actual);
        actual = MonitoringStatus.computeStatus(MonitoringStatus.ERROR, MonitoringStatus.ERROR);
        assertEquals(MonitoringStatus.ERROR, actual);
    }


    @Test
    public void computeStatusCurrentWARN() {
        String actual = MonitoringStatus.computeStatus(MonitoringStatus.WARN, MonitoringStatus.OK);
        assertEquals(MonitoringStatus.WARN, actual);
        actual = MonitoringStatus.computeStatus(MonitoringStatus.WARN, MonitoringStatus.WARN);
        assertEquals(MonitoringStatus.WARN, actual);
        actual = MonitoringStatus.computeStatus(MonitoringStatus.WARN, MonitoringStatus.ERROR);
        assertEquals(MonitoringStatus.ERROR, actual);
    }

    @Test
    public void computeStatusCurrentOK() {
        String actual = MonitoringStatus.computeStatus(MonitoringStatus.OK, MonitoringStatus.OK);
        assertEquals(MonitoringStatus.OK, actual);
        actual = MonitoringStatus.computeStatus(MonitoringStatus.OK, MonitoringStatus.WARN);
        assertEquals(MonitoringStatus.WARN, actual);
        actual = MonitoringStatus.computeStatus(MonitoringStatus.OK, MonitoringStatus.ERROR);
        assertEquals(MonitoringStatus.ERROR, actual);
    }

    @Test
    public void computeStatusNewNull() {
        String actual = MonitoringStatus.computeStatus(MonitoringStatus.ERROR, null);
        assertEquals(MonitoringStatus.ERROR, actual);
        actual = MonitoringStatus.computeStatus(MonitoringStatus.WARN, null);
        assertEquals(MonitoringStatus.WARN, actual);
        actual = MonitoringStatus.computeStatus(MonitoringStatus.OK, null);
        assertEquals(MonitoringStatus.OK, actual);
    }

    @Test
    public void computeStatusCurrentNULL() {
        String actual = MonitoringStatus.computeStatus(null, MonitoringStatus.OK);
        assertEquals(MonitoringStatus.OK, actual);
        actual = MonitoringStatus.computeStatus(null, MonitoringStatus.WARN);
        assertEquals(MonitoringStatus.WARN, actual);
        actual = MonitoringStatus.computeStatus(null, MonitoringStatus.ERROR);
        assertEquals(MonitoringStatus.ERROR, actual);
    }

    @Test
    public void computeStatusCurrentAndNewNULL() {
        String actual = MonitoringStatus.computeStatus(null, null);
        assertNull(actual);
    }
}