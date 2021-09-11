package com.quasiris.qsf.monitoring;

import com.quasiris.qsf.commons.util.QsfInstant;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A builder to create monitoring instances.
 */
public class MonitoringBuilder {

    private List<MonitoringDocument> monitoringDocuments = new ArrayList<>();

    private boolean active = true;

    private Instant pausedUntil;

    protected MonitoringBuilder() {
    }

    /**
     * Create a new instance of the {@link MonitoringBuilder}
     * @return the {@link MonitoringBuilder}
     */
    public static MonitoringBuilder aMonitoring() {
        return new MonitoringBuilder();
    }

    /**
     * Create a new monitoring for the total hits.
     *
     * @param error minimum error limit.
     * @param warn minimum warn limit.
     * @return the {@link MonitoringBuilder}
     */
    public MonitoringBuilder totalHits(long error, long warn) {
        MonitoringDocument<Long> monitoringDocument = MonitoringDocumentBuilder.
                aMonitoringLong().
                withMonitoringId("totalHits").
                withName("Total Hits").
                withType("total").
                withMinErrorLimit(error).
                withMinWarnLimit(warn).build();

        this.monitoringDocuments.add(monitoringDocument);
        return this;

    }

    public MonitoringBuilder active(boolean active) {
        this.active = active;
        return this;
    }


    public MonitoringBuilder pausedUntil(Instant pausedUntil) {
        this.pausedUntil = pausedUntil;
        return this;
    }


    public MonitoringBuilder pausedUntil(Date pausedUntil) {
        this.pausedUntil = Instant.ofEpochMilli(pausedUntil.getTime());
        return this;
    }



    /**
     * Create a new monitoring for a update feed of the processing time of a document.
     * The monitoring query must be sorted descending by the processingtime.
     * The value of the first document is used to compare the processingtime.
     *
     * The name of the field must be processingtime.
     *
     * @param error the maximum age in hours for the error limit.
     * @param warn the maximum age in hours for the warn limit.
     * @param active flag to active or deactivate the monitoring.
     * @return the {@link MonitoringBuilder}
     */
    public MonitoringBuilder processingTimeUpdate(int error, int warn, boolean active) {
        MonitoringDocument<Instant> monitoringDocument = MonitoringProcessingTimeBuilder.
                aMonitoringDocument().
                withProcessingTimeUpdate().
                withErrorLimitHours(error).
                withWarnLimitHours(warn).
                active(active).
                build();
        this.monitoringDocuments.add(monitoringDocument);
        return this;
    }

    /**
     * Create a new monitoring for a update feed of the processing time of a document.
     * The monitoring query must be sorted ascending by the processingtime.
     * The value of the first document is used to compare the processingtime.
     *
     * The name of the field must be processingtime.
     *
     * @param error the maximum age in hours for the error limit.
     * @param warn the maximum age in hours for the warn limit.
     * @return the {@link MonitoringBuilder}
     */
    public MonitoringBuilder processingTimeUpdate(int error, int warn) {
        return processingTimeUpdate(error, warn, true);
    }

    /**
     * Create a new monitoring for a full feed of the processing time of a document.
     * The monitoring query must be sorted ascending by the processingtime.
     * The value of the first document is used to compare the processingtime.
     *
     * The name of the field must be processingtime.
     *
     * @param error the maximum age in hours for the error limit.
     * @param warn the maximum age in hours for the warn limit.
     * @param active flag to active or deactivate the monitoring.
     * @return the {@link MonitoringBuilder}
     */
    public MonitoringBuilder processingTimeFull(int error, int warn, boolean active) {
        MonitoringDocument<Instant> monitoringDocument = MonitoringProcessingTimeBuilder.
                aMonitoringDocument().
                withProcessingTimeFull().
                withErrorLimitHours(error).
                withWarnLimitHours(warn).
                active(active).
                build();
        this.monitoringDocuments.add(monitoringDocument);
        return this;
    }

    /**
     * Create a new monitoring for a full feed of the processing time of a document.
     * The monitoring query must be sorted ascending by the processingtime.
     * The value of the first document is used to compare the processingtime.
     *
     * The name of the field must be processingtime.
     *
     * @param error the maximum age in hours for the error limit.
     * @param warn the maximum age in hours for the warn limit.
     * @return the {@link MonitoringBuilder}
     */
    public MonitoringBuilder processingTimeFull(int error, int warn) {
        return processingTimeFull(error, warn, true);
    }


    /**
     * Create a new monitoring of the processing time of a document.
     * The monitoring query must be sorted ascending by the processingtime.
     * The value of the first document is used to compare the processingtime.
     *
     * The name of the field must be processingtime.
     *
     * @param error the maximum age in hours for the error limit.
     * @param warn the maximum age in hours for the warn limit.
     * @param active flag to active or deactivate the monitoring.
     * @return the {@link MonitoringBuilder}
     * @deprecated use processingTimeFull instead
     */
    public MonitoringBuilder processingTime(int error, int warn, boolean active) {
        MonitoringDocument<Instant> monitoringDocument = MonitoringProcessingTimeBuilder.
            aMonitoringDocument().
            withErrorLimitHours(error).
            withWarnLimitHours(warn).
            active(active).
            build();
        this.monitoringDocuments.add(monitoringDocument);
        return this;
    }

    /**
     * Create a new monitoring of the processing time of a document.
     * The monitoring query must be sorted ascending by the processingtime.
     * The value of the first document is used to compare the processingtime.
     *
     * The name of the field must be processingtime.
     *
     * @param error the maximum age in hours for the error limit.
     * @param warn the maximum age in hours for the warn limit.
     * @return the {@link MonitoringBuilder}
     * @deprecated use processingTimeFull instead
     */
    public MonitoringBuilder processingTime(int error, int warn) {
        return processingTime(error, warn, true);
    }

    /**
     * Create a new monitoring of a facet value.
     *
     * @param fieldName The field name of the facet. Is used to build the facet.
     * @param facetValue The value of the facet, which is relevant for the monitoring.
     * @param error minimum error limit.
     * @param warn minimum warn limit.
     * @return the {@link MonitoringBuilder}
     */
    public MonitoringBuilder facetValue(String fieldName, String facetValue, long error, long warn) {
        MonitoringDocument<Long> monitoringDocument = MonitoringDocumentBuilder.
                aMonitoringLong().
                withMinWarnLimit(warn).
                withMinErrorLimit(error).
                withMonitoringId(fieldName + "." + facetValue).
                withName(fieldName + ": " + facetValue).
                withType("facet").
                withFieldName(fieldName).
                withFacetValue(facetValue).
                build();
        this.monitoringDocuments.add(monitoringDocument);
        return this;
    }


    /**
     * Add a new monitoringDocument.
     * @param monitoringDocument the monitoringDocument to add.
     * @return the {@link MonitoringBuilder}
     */
    public MonitoringBuilder add(MonitoringDocument monitoringDocument) {
        this.monitoringDocuments.add(monitoringDocument);
        return this;

    }

    /**
     * Build the monitoring and return a list of all monitoringDocuments.
     * @return a list of all monitoringDocuments.
     */
    public List<MonitoringDocument> build() {
        if(pausedUntil != null && pausedUntil.isBefore(QsfInstant.now())) {
            this.active = false;
        }
        monitoringDocuments.stream().forEach(d -> d.setActive(active));
        return monitoringDocuments;
    }
}
