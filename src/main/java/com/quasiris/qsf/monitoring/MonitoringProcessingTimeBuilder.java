package com.quasiris.qsf.monitoring;

import com.quasiris.qsf.commons.util.QsfInstant;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/**
 * A Builder to create a monitoring for the processingtime.
 */
public class MonitoringProcessingTimeBuilder extends MonitoringDocumentBuilder<Instant> {

    /** Do not instantiate MonitoringProcessingTimeBuilder. */
    private MonitoringProcessingTimeBuilder() {
    }

    /**
     * Creates a new instance of the MonitoringProcessingTimeBuilder and initialize some default values.
     * @return MonitoringProcessingTimeBuilder
     */
    public static MonitoringProcessingTimeBuilder aMonitoringDocument() {
        MonitoringProcessingTimeBuilder monitoringProcessingTimeBuilder = new MonitoringProcessingTimeBuilder();
        monitoringProcessingTimeBuilder.
                withProcessingTimeFull().
                withFieldName("processingtime");
        return monitoringProcessingTimeBuilder;
    }

    /**
     * Use a full feed processing time monitoring.
     * @return MonitoringProcessingTimeBuilder
     */
    public MonitoringProcessingTimeBuilder withProcessingTimeFull() {
        withType("processingTimeFull");
        withMonitoringId("proccessingTimeFull");
        withName("Processing Time Full");
        return this;
    }

    /**
     * Use a update feed processing time monitoring.
     * @return MonitoringProcessingTimeBuilder
     */
    public MonitoringProcessingTimeBuilder withProcessingTimeUpdate() {
        withType("processingTimeUpdate");
        withMonitoringId("proccessingTimeUpdate");
        withName("Processing Time Update");
        return this;
    }

    /**
     * @param hours the maximum age in hours for the error limit.
     * @return MonitoringProcessingTimeBuilder
     */
    public MonitoringProcessingTimeBuilder withErrorLimitHours(int hours) {
        withErrorLimit(hours, ChronoUnit.HOURS);
        return this;
    }

    /**
     * @param hours the maximum age in hours for the warn limit.
     * @return MonitoringProcessingTimeBuilder
     */
    public MonitoringProcessingTimeBuilder withWarnLimitHours(int hours) {
        withWarnLimit(hours, ChronoUnit.HOURS);
        return this;
    }


    /**
     * @param value the maximum age in given temporalUnit for the error limit.
     * @param temporalUnit the temporal unit
     * @return MonitoringProcessingTimeBuilder
     */
    public MonitoringProcessingTimeBuilder withErrorLimit(int value, TemporalUnit temporalUnit) {
        withMinErrorLimit(getIntant(value, temporalUnit));
        return this;
    }

    /**
     * @param value the maximum age in given temporalUnit for the warn limit.
     * @param temporalUnit the temporal unit
     * @return MonitoringProcessingTimeBuilder
     */
    public MonitoringProcessingTimeBuilder withWarnLimit(int value, TemporalUnit temporalUnit) {
        withMinWarnLimit(getIntant(value, temporalUnit));
        return this;
    }

    /**
     * Create a new instant based on the given age and temporal unit.
     *
     * @param value the age
     * @param temporalUnit the temporal unit
     * @return the specific instant for the given age and temporal unit.
     */
    private Instant getIntant(int value, TemporalUnit temporalUnit) {
        return QsfInstant.now().minus(value, temporalUnit);
    }

}
