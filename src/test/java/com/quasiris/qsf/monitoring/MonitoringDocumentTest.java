package com.quasiris.qsf.monitoring;

import com.quasiris.qsf.util.DateUtil;
import com.quasiris.qsf.util.QsfInstant;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;


public class MonitoringDocumentTest {


    @Test
    public void testLongOKWithNoLimits() {
        MonitoringDocument<Long> monitoringDocument = new MonitoringDocument<>();
        monitoringDocument.setValue(5L);
        monitoringDocument.check();

        assertFalse(monitoringDocument.isWarn());
        assertFalse(monitoringDocument.isError());
        assertTrue(monitoringDocument.isOk());
        assertEquals("OK", monitoringDocument.getStatus());
    }

    @Test
    public void testLongOKWithMinLimits() {
        MonitoringDocument<Long> monitoringDocument = new MonitoringDocument<>();
        monitoringDocument.setValue(5L);
        monitoringDocument.setMinWarnLimit(4L);
        monitoringDocument.setMinErrorLimit(3L);
        monitoringDocument.check();

        assertFalse(monitoringDocument.isWarn());
        assertFalse(monitoringDocument.isError());
        assertTrue(monitoringDocument.isOk());
        assertEquals("OK", monitoringDocument.getStatus());
    }

    @Test
    public void testLongOKWithMaxLimits() {
        MonitoringDocument<Long> monitoringDocument = new MonitoringDocument<>();
        monitoringDocument.setValue(5L);
        monitoringDocument.setMaxWarnLimit(6L);
        monitoringDocument.setMaxErrorLimit(7L);
        monitoringDocument.check();

        assertFalse(monitoringDocument.isWarn());
        assertFalse(monitoringDocument.isError());
        assertTrue(monitoringDocument.isOk());
        assertEquals("OK", monitoringDocument.getStatus());
    }

    @Test
    public void testLongWarnWithMaxLimits() {
        MonitoringDocument<Long> monitoringDocument = new MonitoringDocument<>();
        monitoringDocument.setValue(7L);
        monitoringDocument.setMaxWarnLimit(6L);
        monitoringDocument.setMaxErrorLimit(7L);
        monitoringDocument.check();

        assertTrue(monitoringDocument.isWarn());
        assertFalse(monitoringDocument.isError());
        assertFalse(monitoringDocument.isOk());
        assertEquals("WARN", monitoringDocument.getStatus());
    }


    @Test
    public void testDateWarnWithMaxLimits() {
        MonitoringDocument<Date> monitoringDocument = new MonitoringDocument<>();
        monitoringDocument.setValue(getDateByString("2019-05-22T10:11:12"));
        monitoringDocument.setMaxWarnLimit(getDateByString("2019-05-22T10:11:00"));
        monitoringDocument.setMaxErrorLimit(getDateByString("2019-05-22T10:11:59"));
        monitoringDocument.check();

        assertTrue(monitoringDocument.isWarn());
        assertFalse(monitoringDocument.isError());
        assertFalse(monitoringDocument.isOk());
        assertEquals("WARN", monitoringDocument.getStatus());
    }

    @Test
    public void testProcessingTimeWarn() {
        QsfInstant.setNow(DateUtil.getInstantByString("2019-05-22T23:11:12Z"));
        MonitoringDocument<Instant> monitoringDocument = MonitoringProcessingTimeBuilder.aMonitoringDocument().
                withWarnLimitHours(2).
                withErrorLimitHours(3).
                withValue(DateUtil.getInstantByString("2019-05-22T20:18:12Z")).
                build();
        monitoringDocument.check();

        assertTrue(monitoringDocument.isWarn());
        assertFalse(monitoringDocument.isError());
        assertFalse(monitoringDocument.isOk());
        assertEquals("WARN", monitoringDocument.getStatus());
    }

    private Date getDateByString(String date) {
        LocalDateTime ld = LocalDateTime.parse(date);
        return Date.from(ld.atZone(ZoneId.systemDefault()).toInstant());
    }



}