package com.quasiris.qsf.monitoring;

import java.time.Instant;

/**
 * A builder to easily create monitoring documents.
 *
 * @param <T> the type of the monitoring document.
 */
public class MonitoringDocumentBuilder<T extends Comparable> {
    private String monitoringId = "monitoringId";
    private String name = "unknown name";
    private String status = "UNKNOWN";
    private String type;
    private String facetValue;
    private String fieldName;
    private T value;
    private T minWarnLimit;
    private T maxWarnLimit;
    private T minErrorLimit;
    private T maxErrorLimit;
    private boolean active = true;


    /** Constructs a new MonitoringDocumentBuilder. */
    protected MonitoringDocumentBuilder() {
    }

    /**
     * @return MonitoringDocumentBuilder for Long
     */
    public static MonitoringDocumentBuilder<Long> aMonitoringLong() {
        return new MonitoringDocumentBuilder<>();
    }

    /**
     * @return MonitoringDocumentBuilder for Instant
     */
    public static MonitoringDocumentBuilder<Instant> aMonitoringInstant() {
        return new MonitoringDocumentBuilder<>();
    }

    /**
     * @param monitoringId the monitoring id
     * @return MonitoringDocumentBuilder
     */
    public MonitoringDocumentBuilder withMonitoringId(String monitoringId) {
        this.monitoringId = monitoringId;
        return this;
    }

    /**
     * @param name A descriptive name for the monitoring.
     * @return MonitoringDocumentBuilder
     */
    public MonitoringDocumentBuilder withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @param status The status of the monitoring.
     * @return MonitoringDocumentBuilder
     */
    public MonitoringDocumentBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    /**
     * @param type The type.
     * @return MonitoringDocumentBuilder
     */
    public MonitoringDocumentBuilder withType(String type) {
        this.type = type;
        return this;
    }

    /**
     * @param fieldName the field name.
     * @return MonitoringDocumentBuilder
     */
    public MonitoringDocumentBuilder withFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    /**
     * @param facetValue the facet value. Only useful for facet monitoring
     * @return MonitoringDocumentBuilder
     */
    public MonitoringDocumentBuilder withFacetValue(String facetValue) {
        this.facetValue = facetValue;
        return this;
    }

    /**
     * @param value the actual value.
     * @return MonitoringDocumentBuilder
     */
    public MonitoringDocumentBuilder withValue(T value) {
        this.value = value;
        return this;
    }

    /**
     * @param minWarnLimit the minimum warn limit.
     * @return MonitoringDocumentBuilder
     */
    public MonitoringDocumentBuilder withMinWarnLimit(T minWarnLimit) {
        this.minWarnLimit = minWarnLimit;
        return this;
    }

    /**
     * @param maxWarnLimit the maximum warn limit.
     * @return MonitoringDocumentBuilder
     */
    public MonitoringDocumentBuilder withMaxWarnLimit(T maxWarnLimit) {
        this.maxWarnLimit = maxWarnLimit;
        return this;
    }

    /**
     * @param minErrorLimit the minimum error limit.
     * @return MonitoringDocumentBuilder
     */
    public MonitoringDocumentBuilder withMinErrorLimit(T minErrorLimit) {
        this.minErrorLimit = minErrorLimit;
        return this;
    }

    /**
     * @param maxErrorLimit the maximum error limit.
     * @return MonitoringDocumentBuilder
     */
    public MonitoringDocumentBuilder withMaxErrorLimit(T maxErrorLimit) {
        this.maxErrorLimit = maxErrorLimit;
        return this;
    }

    /**
     *
     * @param active the active flag.
     * @return MonitoringDocumentBuilder
     */
    public MonitoringDocumentBuilder active(boolean active) {
        this.active = active;
        return this;
    }


    /**
     * Creates the MonitoringDocument.
     * @return the builded MonitoringDocument
     */
    public MonitoringDocument build() {
        MonitoringDocument<T> monitoringDocument = new MonitoringDocument<>();
        monitoringDocument.setMonitoringId(monitoringId);
        monitoringDocument.setName(name);
        monitoringDocument.setStatus(status);
        monitoringDocument.setFacetValue(facetValue);
        monitoringDocument.setFieldName(fieldName);
        monitoringDocument.setValue(value);
        monitoringDocument.setType(type);
        monitoringDocument.setMinWarnLimit(minWarnLimit);
        monitoringDocument.setMaxWarnLimit(maxWarnLimit);
        monitoringDocument.setMinErrorLimit(minErrorLimit);
        monitoringDocument.setMaxErrorLimit(maxErrorLimit);
        monitoringDocument.setActive(active);
        return monitoringDocument;
    }
}
