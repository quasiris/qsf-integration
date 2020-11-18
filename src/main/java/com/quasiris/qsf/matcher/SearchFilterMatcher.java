package com.quasiris.qsf.matcher;

import com.quasiris.qsf.query.RangeFilterValue;
import com.quasiris.qsf.query.SearchFilter;

public class SearchFilterMatcher {


    public boolean matches(SearchFilter searchFilter, String value) {
        for(String v : searchFilter.getValues()) {
            if(v.equals(value)) {
                return true;
            }
        }
        return false;

    }

    public boolean matchesRangeValue(SearchFilter searchFilter, Double value) {
        if(value == null) {
            return false;
        }
        RangeFilterValue<Double> rangeValue = searchFilter.getRangeValue(Double.class);
        Double minValue = rangeValue.getMinValue();
        Double maxValue = rangeValue.getMaxValue();

        boolean matches = false;
        if(minValue == null && maxValue == null) {
            matches = true;
        } else if(searchFilter.isUpperExcluded() && minValue == null) {
            if(value < maxValue) {
                matches = true;
            }
        } else if(searchFilter.isUpperIncluded() && minValue == null) {
            if(value <= maxValue) {
                matches = true;
            }
        } else if(searchFilter.isLowerExcluded() && maxValue == null) {
            if(value > minValue) {
                matches = true;
            }
        } else if(searchFilter.isLowerIncluded() && maxValue == null ) {
            if(value >= minValue) {
                matches = true;
            }
        } else if(searchFilter.isLowerExcluded() && searchFilter.isUpperExcluded()) {
            if(value > minValue && value < maxValue) {
                matches = true;
            }
        } else if(searchFilter.isLowerIncluded() && searchFilter.isUpperIncluded()) {
            if(value >= minValue && value <= maxValue) {
                matches = true;
            }
        } else if(searchFilter.isLowerIncluded() && searchFilter.isUpperExcluded()) {
            if(value >= minValue && value < maxValue) {
                matches = true;
            }
        } else if(searchFilter.isLowerExcluded() && searchFilter.isUpperIncluded()) {
            if(value > minValue && value <= maxValue) {
                matches = true;
            }
        }
        return matches;
    }
}
