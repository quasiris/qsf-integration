package com.quasiris.qsf.test.dto;

public class JsonPath {

    /**
     * The JSON key of the field in which the raw JSON that should be tested for is stored
     */
    private String fieldName;

    /**
     * A Jayway JsonPath to determine the key path for which the value in the raw JSON should be tested <br>
     * See <a href="https://github.com/json-path/JsonPath#operators">Jayway JsonPath on Github</a> for info how to build the path
     */
    private String path;

    /**
     * A value for which should be tested. Can contain an {@link com.quasiris.qsf.test.service.Operator}
     */
    private String value;


    /**
     * Getter for property 'fieldName'.
     *
     * @return Value for property 'fieldName'.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Setter for property 'fieldName'.
     *
     * @param fieldName Value to set for property 'fieldName'.
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Getter for property 'path'.
     *
     * @return Value for property 'path'.
     */
    public String getPath() {
        return path;
    }

    /**
     * Setter for property 'path'.
     *
     * @param path Value to set for property 'path'.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Getter for property 'value'.
     *
     * @return Value for property 'value'.
     */
    public String getValue() {
        return value;
    }

    /**
     * Setter for property 'value'.
     *
     * @param value Value to set for property 'value'.
     */
    public void setValue(String value) {
        this.value = value;
    }
}
