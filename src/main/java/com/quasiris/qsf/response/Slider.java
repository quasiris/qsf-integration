package com.quasiris.qsf.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by tbl on 4.7.19.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Slider {

    private String name;

    private String id;

    private String filterName;

    private Long count;

    private Double minValue;
    private Double maxValue;

    private Double minRange;
    private Double maxRange;

    private Boolean selected;

    /**
     * Getter for property 'name'.
     *
     * @return Value for property 'name'.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for property 'name'.
     *
     * @param name Value to set for property 'name'.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for property 'id'.
     *
     * @return Value for property 'id'.
     */
    public String getId() {
        return id;
    }

    /**
     * Setter for property 'id'.
     *
     * @param id Value to set for property 'id'.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for property 'filterName'.
     *
     * @return Value for property 'filterName'.
     */
    public String getFilterName() {
        return filterName;
    }

    /**
     * Setter for property 'filterName'.
     *
     * @param filterName Value to set for property 'filterName'.
     */
    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    /**
     * Getter for property 'count'.
     *
     * @return Value for property 'count'.
     */
    public Long getCount() {
        return count;
    }

    /**
     * Setter for property 'count'.
     *
     * @param count Value to set for property 'count'.
     */
    public void setCount(Long count) {
        this.count = count;
    }


    /**
     * Getter for property 'selected'.
     *
     * @return Value for property 'selected'.
     */
    public Boolean getSelected() {
        return selected;
    }

    /**
     * Setter for property 'selected'.
     *
     * @param selected Value to set for property 'selected'.
     */
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }


    /**
     * Getter for property 'minValue'.
     *
     * @return Value for property 'minValue'.
     */
    public Double getMinValue() {
        return minValue;
    }

    /**
     * Setter for property 'minValue'.
     *
     * @param minValue Value to set for property 'minValue'.
     */
    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public void setMinValueOrDefault(Double minValue) {
        if(minValue == null) {
            minValue = minRange;
        }
        this.minValue = minValue;
    }

    public void setMaxValueOrDefault(Double maxValue) {
        if(maxValue == null) {
            maxValue = maxRange;
        }
        this.maxValue = maxValue;
    }

    /**
     * Getter for property 'maxValue'.
     *
     * @return Value for property 'maxValue'.
     */
    public Double getMaxValue() {
        return maxValue;
    }

    /**
     * Setter for property 'maxValue'.
     *
     * @param maxValue Value to set for property 'maxValue'.
     */
    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    /**
     * Getter for property 'minRange'.
     *
     * @return Value for property 'minRange'.
     */
    public Double getMinRange() {
        return minRange;
    }

    /**
     * Setter for property 'minRange'.
     *
     * @param minRange Value to set for property 'minRange'.
     */
    public void setMinRange(Double minRange) {
        this.minRange = minRange;
    }

    /**
     * Getter for property 'maxRange'.
     *
     * @return Value for property 'maxRange'.
     */
    public Double getMaxRange() {
        return maxRange;
    }

    /**
     * Setter for property 'maxRange'.
     *
     * @param maxRange Value to set for property 'maxRange'.
     */
    public void setMaxRange(Double maxRange) {
        this.maxRange = maxRange;
    }

    @Override
    public String toString() {
        return "Slider{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", filterName='" + filterName + '\'' +
                ", count=" + count +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                ", minRange=" + minRange +
                ", maxRange=" + maxRange +
                ", selected=" + selected +
                '}';
    }
}
