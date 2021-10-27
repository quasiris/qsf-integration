package com.quasiris.qsf.monitoring;

import com.quasiris.qsf.TestHelper;
import com.quasiris.qsf.commons.util.QsfInstant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

class MonitoringBuilderTest {

    @Test
    public void initMonitoringCheckSize() {
        List<MonitoringDocument> actual = MonitoringBuilder.aMonitoring()
                .active(true)
                .totalHits(20, 200)
                .facetValue("genre", "fantasy", 20, 30)
                .pausedUntil(new Date().toInstant().plus(Duration.ofSeconds(2)))
                .processingTimeFull(20, 200)
                .processingTimeUpdate(200, 100)
                .build();
        Assertions.assertEquals(4, actual.size());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void initMonitoringCheckSize(boolean expected) {
        List<MonitoringDocument> actual = MonitoringBuilder.aMonitoring()
                .active(expected)
                .totalHits(20, 200)
                .facetValue("genre", "fantasy", 20, 30)
                .pausedUntil(new Date().toInstant().plus(Duration.ofSeconds(2)))
                .processingTimeFull(20, 200)
                .processingTimeUpdate(200, 100)
                .build();
        Assertions.assertTrue(actual.stream().allMatch(d -> d.isActive() == expected));
    }

    @Test
    public void initMonitoringTotalHitsPausedUntilDate() {
        long error = 20;
        long warn = 200;
        Instant warnInstant = Instant.now().minus(Duration.ofHours(warn));
        Instant errorInstant = Instant.now().minus(Duration.ofHours(error));
        List<MonitoringDocument> actual = MonitoringBuilder.aMonitoring()
                .totalHits(error, warn)
                .pausedUntil(Date.from(new Date().toInstant().minus(Duration.ofSeconds(2))))
                .build();
        System.out.println("actual = " + actual);
        MonitoringDocument monitoringDoc = actual.get(0);
        Assertions.assertEquals("total", monitoringDoc.getType());
        Assertions.assertEquals("Long", monitoringDoc.getDataType());
        Assertions.assertEquals(error, monitoringDoc.getMinErrorLimit());
        Assertions.assertEquals(warn, monitoringDoc.getMinWarnLimit());
    }

    @Test
    public void initMonitoringProcessingActiveInline() {
        int warn = 1000;
        int error = 10;
        List<MonitoringDocument> actual = MonitoringBuilder.aMonitoring()
                .processingTime(error, warn, false)
                .build();
        Assertions.assertEquals(1, actual.size());
        MonitoringDocument doc = actual.get(0);
        Instant warnInstant = QsfInstant.now().minus(warn, ChronoUnit.HOURS);
        Instant errorInstant = QsfInstant.now().minus(error, ChronoUnit.HOURS);
        TestHelper.checkNearlySameTime((Instant) doc.getMinWarnLimit(), warnInstant, 1000);
        TestHelper.checkNearlySameTime((Instant) doc.getMinErrorLimit(), errorInstant, 1000);
    }

}