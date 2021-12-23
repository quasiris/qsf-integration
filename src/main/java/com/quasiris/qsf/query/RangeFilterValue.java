package com.quasiris.qsf.query;

import java.io.Serializable;

/**
 * Created by mki on 11.11.16.
 */
public class RangeFilterValue<T> implements Serializable {


    public RangeFilterValue() {
    }

    public RangeFilterValue(T minValue, T maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    private T minValue;
    private T maxValue;

    private UpperLowerBound lowerBound = UpperLowerBound.LOWER_INCLUDED;
    private UpperLowerBound upperBound = UpperLowerBound.UPPER_INCLUDED;


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

    public UpperLowerBound getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(UpperLowerBound lowerBound) {
        this.lowerBound = lowerBound;
    }

    public UpperLowerBound getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(UpperLowerBound upperBound) {
        this.upperBound = upperBound;
    }

    @Override
    public String toString() {
        return "RangeFilterValue{" +
                "minValue=" + minValue +
                ", maxValue=" + maxValue +
                ", lowerBound=" + lowerBound +
                ", upperBound=" + upperBound +
                '}';
    }
}
