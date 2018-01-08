package com.quasiris.qsf.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by mki on 12.11.17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FacetValue {

    public FacetValue(String value, Long count) {
        this.value = value;
        this.count = count;
    }

    private String value;

    private Long count;

    private String filter;

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

    @Override
    public String toString() {
        return "FacetValue{" +
                "value='" + value + '\'' +
                ", count=" + count +
                ", filter='" + filter + '\'' +
                '}';
    }
}
