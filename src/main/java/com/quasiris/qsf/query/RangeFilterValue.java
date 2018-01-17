package com.quasiris.qsf.query;

/**
 * Created by mki on 11.11.16.
 */
public class RangeFilterValue<T> {


    public RangeFilterValue() {
    }

    public RangeFilterValue(T minValue, T maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    private T minValue;
    private T maxValue;


    public T getMinValue() {
        return minValue;
    }

    public void setMinValue(T minValue) {
        this.minValue = minValue;
    }

    public T getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(T maxValue) {
        this.maxValue = maxValue;
    }
}
