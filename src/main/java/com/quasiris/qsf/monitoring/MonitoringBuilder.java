package com.quasiris.qsf.monitoring;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A builder to create monitoring instances.
 */
public class MonitoringBuilder {

    private List<MonitoringDocument> monitoringDocuments = new ArrayList<>();

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
     */
    public MonitoringBuilder processingTime(int error, int warn) {
        MonitoringDocument<Instant> monitoringDocument = MonitoringProcessingTimeBuilder.
                aMonitoringDocument().
                withErrorLimitHours(error).
                withWarnLimitHours(warn).
                withMonitoringId("processingTime").
                withName("Processing Time").
                withType("document").
                build();
        this.monitoringDocuments.add(monitoringDocument);
        return this;
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
        return monitoringDocuments;
    }
}
