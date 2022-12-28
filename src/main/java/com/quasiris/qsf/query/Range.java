package com.quasiris.qsf.query;

import java.io.Serializable;

public class Range implements Serializable {

    public Range(String value, Object min, Object max) {
        this.value = value;
        this.min = min;
        this.max = max;

    }
    private String value;
    private Object min;

    private Object max;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Object getMin() {
        return min;
    }

    public void setMin(Object min) {
        this.min = min;
    }

    public Object getMax() {
        return max;
    }

    public void setMax(Object max) {
        this.max = max;
    }
}
