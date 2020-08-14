package com.quasiris.qsf.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by mki on 12.11.17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FacetValue {

    public FacetValue() {
    }

    public FacetValue(String value, Long count) {
        this.value = value;
        this.count = count;
    }

    private String value;

    private Long count;

    private String filter;

    private Boolean selected;

    private Facet children;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * Getter for property 'children'.
     *
     * @return Value for property 'children'.
     */
    public Facet getChildren() {
        return children;
    }

    /**
     * Setter for property 'children'.
     *
     * @param children Value to set for property 'children'.
     */
    public void setChildren(Facet children) {
        this.children = children;
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
        return "FacetValue{" +
                "value='" + value + '\'' +
                ", count=" + count +
                ", filter='" + filter + '\'' +
                ", children=" + children +
                '}';
    }
}
