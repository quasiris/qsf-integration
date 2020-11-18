package com.quasiris.qsf.query;

import java.util.ArrayList;
import java.util.List;

public final class SearchFilterBuilder {
    private FilterType filterType = FilterType.TERM;
    private FilterOperator filterOperator = FilterOperator.AND;
    private FilterDataType filterDataType = FilterDataType.STRING;
    private boolean exclude = true;
    private String id;
    private String name;
    private List<String> values;
    private RangeFilterValue<?> rangeValue;
    private UpperLowerBound lowerBound = UpperLowerBound.LOWER_INCLUDED;
    private UpperLowerBound upperBound = UpperLowerBound.UPPER_INCLUDED;

    private SearchFilterBuilder() {
    }

    public static SearchFilterBuilder create() {
        return new SearchFilterBuilder();
    }

    public SearchFilterBuilder rangeFilter(Double min, Double max) {
        rangeValue = new RangeFilterValue<>(min, max);
        filterType = FilterType.RANGE;
        filterDataType = FilterDataType.NUMBER;
        return this;
    }


    public SearchFilterBuilder rangeFilter(String min, String max) {
        rangeValue = new RangeFilterValue<>(min, max);
        filterType = FilterType.RANGE;
        filterDataType = FilterDataType.STRING;
        return this;
    }

    public SearchFilterBuilder values(List<String> values) {
        this.values = values;
        return this;
    }

    public SearchFilterBuilder value(String value) {
        if(this.values == null) {
            this.values = new ArrayList<>();
        }
        this.values.add(value);
        return this;
    }

    public SearchFilterBuilder withFilterType(FilterType filterType) {
        this.filterType = filterType;
        return this;
    }

    public SearchFilterBuilder withFilterOperator(FilterOperator filterOperator) {
        this.filterOperator = filterOperator;
        return this;
    }

    public SearchFilterBuilder withFilterDataType(FilterDataType filterDataType) {
        this.filterDataType = filterDataType;
        return this;
    }

    public SearchFilterBuilder withExclude(boolean exclude) {
        this.exclude = exclude;
        return this;
    }

    public SearchFilterBuilder withLowerBoundInclude() {
        this.lowerBound = UpperLowerBound.LOWER_INCLUDED;
        return this;
    }

    public SearchFilterBuilder withLowerBoundExclude() {
        this.lowerBound = UpperLowerBound.LOWER_EXCLUDED;
        return this;
    }

    public SearchFilterBuilder withUpperBoundInclude() {
        this.upperBound = UpperLowerBound.UPPER_INCLUDED;
        return this;
    }

    public SearchFilterBuilder withUpperBoundExclude() {
        this.upperBound = UpperLowerBound.UPPER_EXCLUDED;
        return this;
    }

    public SearchFilterBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public SearchFilterBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public SearchFilterBuilder withValues(List<String> values) {
        this.values = values;
        return this;
    }

    public SearchFilterBuilder withRangeValue(RangeFilterValue<?> rangeValue) {
        this.rangeValue = rangeValue;
        return this;
    }

    public SearchFilter build() {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.setFilterType(filterType);
        searchFilter.setFilterOperator(filterOperator);
        searchFilter.setFilterDataType(filterDataType);
        searchFilter.setExclude(exclude);
        searchFilter.setId(id);
        searchFilter.setName(name);
        searchFilter.setValues(values);

        if(rangeValue != null) {
            this.rangeValue.setLowerBound(lowerBound);
            this.rangeValue.setUpperBound(upperBound);
        }
        searchFilter.setRangeValue(rangeValue);
        return searchFilter;
    }
}
