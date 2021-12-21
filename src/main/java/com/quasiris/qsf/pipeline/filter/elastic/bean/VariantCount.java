package com.quasiris.qsf.pipeline.filter.elastic.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by mki on 04.02.18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VariantCount {

    private Long value;

    /**
     * Getter for property 'value'.
     *
     * @return Value for property 'value'.
     */
    public Long getValue() {
        return value;
    }

    /**
     * Setter for property 'value'.
     *
     * @param value Value to set for property 'value'.
     */
    public void setValue(Long value) {
        this.value = value;
    }
}
