package com.quasiris.qsf.monitoring;

import com.quasiris.qsf.response.Document;

/**
 * Created by tbl on 22.05.19.
 * @param <T> The type of the monitoring document. E.g. Long, Instant, ...
 */
public class MonitoringDocument<T extends Comparable> extends Document {


    private String monitoringId = "monitoringId";
    private String name = "name";
    private String status = "status";
    private String value = "value";

    /*
     * total
     * document
     * facet
     *
     */
    private String type = "type";
    private String dataType = "dataType";

    private String fieldName = "fieldName";
    private String facetValue = "facetValue";
    private String minWarnLimit = "minWarnLimit";
    private String maxWarnLimit = "maxWarnLimit";
    private String minErrorLimit = "minErrorLimit";
    private String maxErrorLimit = "maxErrorLimit";


    /**
     * Getter for property 'monitoringId'.
     *
     * @return Value for property 'monitoringId'.
     */
    public String getMonitoringId() {
        return getFieldValue(monitoringId);
    }

    /**
     * Setter for property 'monitoringId'.
     *
     * @param monitoringId Value to set for property 'monitoringId'.
     */
    public void setMonitoringId(String monitoringId) {
        setValueIfNotNull(this.monitoringId, monitoringId);
    }

    /**
     * Getter for property 'name'.
     *
     * @return Value for property 'name'.
     */
    public String getName() {
        return getFieldValue(name);
    }

    /**
     * Setter for property 'name'.
     *
     * @param name Value to set for property 'name'.
     */
    public void setName(String name) {
        setValueIfNotNull(this.name, name);
    }

    /**
     * Getter for property 'status'.
     *
     * @return Value for property 'status'.
     */
    public String getStatus() {
        return getFieldValue(status);
    }

    /**
     * Setter for property 'status'.
     *
     * @param status Value to set for property 'status'.
     */
    public void setStatus(String status) {
        setValueIfNotNull(this.status, status);
    }

    /**
     * Getter for property 'type'.
     *
     * @return Value for property 'type'.
     */
    public String getType() {
        return getFieldValue(type);
    }

    /**
     * Setter for property 'type'.
     *
     * @param type Value to set for property 'type'.
     */
    public void setType(String type) {
        setValueIfNotNull(this.type, type);
    }

    /**
     * Getter for property 'dataType'.
     *
     * @return Value for property 'dataType'.
     */
    public String getDataType() {
        return getFieldValue(dataType);
    }

    /**
     * Setter for property 'dataType'.
     *
     * @param dataType Value to set for property 'dataType'.
     */
    public void setDataType(String dataType) {
        setValueIfNotNull(this.dataType, dataType);
    }

    /**
     * Setter for property 'dataTypeForObject'.
     *
     * @param value Value to set for property 'dataTypeForObject'.
     */
    private void setDataTypeForObject(Object value) {
        if(value != null) {
            setDataType(value.getClass().getSimpleName());
        }
    }

    /**
     * Getter for property 'fieldName'.
     *
     * @return Value for property 'fieldName'.
     */
    public String getFieldName() {
        return getFieldValue(fieldName);
    }

    /**
     * Setter for property 'fieldName'.
     *
     * @param fieldName Value to set for property 'fieldName'.
     */
    public void setFieldName(String fieldName) {
        setValueIfNotNull(this.fieldName, fieldName);
    }

    /**
     * Getter for property 'facetValue'.
     *
     * @return Value for property 'facetValue'.
     */
    public String getFacetValue() {
        return getFieldValue(facetValue);
    }

    /**
     * Setter for property 'facetValue'.
     *
     * @param facetValue Value to set for property 'facetValue'.
     */
    public void setFacetValue(String facetValue) {
        setValueIfNotNull(this.facetValue, facetValue);
    }

    /**
     * Getter for property 'value'.
     *
     * @return Value for property 'value'.
     */
    public T getValue() {
        return (T) getFieldValueAsObject(this.value);
    }

    /**
     * Setter for property 'value'.
     *
     * @param value Value to set for property 'value'.
     */
    public void setValue(T value) {
        setValueIfNotNull(this.value, value);
    }

    /**
     * Getter for property 'minWarnLimit'.
     *
     * @return Value for property 'minWarnLimit'.
     */
    public T getMinWarnLimit() {
        return (T) getFieldValueAsObject(this.minWarnLimit);
    }

    /**
     * Setter for property 'minWarnLimit'.
     *
     * @param minWarnLimitValue Value to set for property 'minWarnLimit'.
     */
    public void setMinWarnLimit(T minWarnLimitValue) {
        setDataTypeForObject(minWarnLimitValue);
        setValueIfNotNull(this.minWarnLimit, minWarnLimitValue);
    }

    /**
     * Getter for property 'maxWarnLimit'.
     *
     * @return Value for property 'maxWarnLimit'.
     */
    public T getMaxWarnLimit() {
        return (T) getFieldValueAsObject(this.maxWarnLimit);
    }

    /**
     * Setter for property 'maxWarnLimit'.
     *
     * @param maxWarnLimitValue Value to set for property 'maxWarnLimit'.
     */
    public void setMaxWarnLimit(T maxWarnLimitValue) {
        setDataTypeForObject(maxWarnLimitValue);
        setValueIfNotNull(this.maxWarnLimit, maxWarnLimitValue);
    }

    /**
     * Getter for property 'minErrorLimit'.
     *
     * @return Value for property 'minErrorLimit'.
     */
    public T getMinErrorLimit() {
        return (T) getFieldValueAsObject(this.minErrorLimit);
    }

    /**
     * Setter for property 'minErrorLimit'.
     *
     * @param minErrorLimitValue Value to set for property 'minErrorLimit'.
     */
    public void setMinErrorLimit(T minErrorLimitValue) {
        setDataTypeForObject(minErrorLimitValue);
        setValueIfNotNull(this.minErrorLimit, minErrorLimitValue);
    }

    /**
     * Getter for property 'maxErrorLimit'.
     *
     * @return Value for property 'maxErrorLimit'.
     */
    public T getMaxErrorLimit() {
        return (T) getFieldValueAsObject(this.maxErrorLimit);
    }

    /**
     * Setter for property 'maxErrorLimit'.
     *
     * @param maxErrorLimitValue Value to set for property 'maxErrorLimit'.
     */
    public void setMaxErrorLimit(T maxErrorLimitValue) {
        setDataTypeForObject(maxErrorLimitValue);
        setValueIfNotNull(this.maxErrorLimit, maxErrorLimitValue);
    }



    /**
     * Getter for property 'warn'.
     *
     * @return Value for property 'warn'.
     */
    public boolean isWarn() {
        if(getMinWarnLimit() != null && (getValue().compareTo(getMinWarnLimit()) < 0)) {
            return true;
        }
        if(getMaxWarnLimit() != null && (getValue().compareTo(getMaxWarnLimit()) > 0)) {
            return true;
        }
        return false;
    }

    /**
     * Getter for property 'error'.
     *
     * @return Value for property 'error'.
     */
    public boolean isError() {
        if(getMinErrorLimit() != null && (getValue().compareTo(getMinErrorLimit()) < 0)) {
            return true;
        }
        if(getMaxErrorLimit() != null && (getValue().compareTo(getMaxErrorLimit()) > 0)) {
            return true;
        }
        return false;
    }

    /**
     * Getter for property 'ok'.
     *
     * @return Value for property 'ok'.
     */
    public boolean isOk() {
        if(isError()) {
            return false;
        }
        if(isWarn()) {
            return false;
        }
        return true;
    }

    /**
     * Check the monitoring results and set the status.
     */
    public void check() {
        if(isError()) {
            setStatus("ERROR");
        } else if (isWarn()) {
            setStatus("WARN");
        } else {
           setStatus("OK");
        }
    }

}
