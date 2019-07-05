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

    private Double min;
    private Double max;

    private Double selectedMin;
    private Double selectedMax;

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
     * Getter for property 'min'.
     *
     * @return Value for property 'min'.
     */
    public Double getMin() {
        return min;
    }

    /**
     * Setter for property 'min'.
     *
     * @param min Value to set for property 'min'.
     */
    public void setMin(Double min) {
        this.min = min;
    }

    /**
     * Getter for property 'max'.
     *
     * @return Value for property 'max'.
     */
    public Double getMax() {
        return max;
    }

    /**
     * Setter for property 'max'.
     *
     * @param max Value to set for property 'max'.
     */
    public void setMax(Double max) {
        this.max = max;
    }

    /**
     * Getter for property 'selectedMin'.
     *
     * @return Value for property 'selectedMin'.
     */
    public Double getSelectedMin() {
        return selectedMin;
    }

    /**
     * Setter for property 'selectedMin'.
     *
     * @param selectedMin Value to set for property 'selectedMin'.
     */
    public void setSelectedMin(Double selectedMin) {
        this.selectedMin = selectedMin;
    }

    /**
     * Getter for property 'selectedMax'.
     *
     * @return Value for property 'selectedMax'.
     */
    public Double getSelectedMax() {
        return selectedMax;
    }

    /**
     * Setter for property 'selectedMax'.
     *
     * @param selectedMax Value to set for property 'selectedMax'.
     */
    public void setSelectedMax(Double selectedMax) {
        this.selectedMax = selectedMax;
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

    @Override
    public String toString() {
        return "Slider{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", filterName='" + filterName + '\'' +
                ", count=" + count +
                ", min=" + min +
                ", max=" + max +
                ", selectedMin=" + selectedMin +
                ", selectedMax=" + selectedMax +
                '}';
    }
}
