package com.quasiris.qsf.query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mki on 11.11.16.
 */
public class SearchFilter {

    private FilterType filterType;

    private FilterOperator filterOperator;

    private FilterDataType filterDataType;

    private boolean exclude = true;

    private String id;

    private String name;

    private List<String> values;

    private RangeFilterValue<?> rangeValue;

    public List<String> getValues() {
        return values;
    }


    public void addValue(String value) {
        if(getValues() == null) {
            this.values = new ArrayList<>();
        }
        getValues().add(value);
    }
    public void setValues(List<String> values) {
        this.values = values;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
        if(FilterType.TERM.equals(filterType)) {
            this.filterDataType = FilterDataType.STRING;
        }
    }

    public FilterOperator getFilterOperator() {
        return filterOperator;
    }

    public void setFilterOperator(FilterOperator filterOperator) {
        this.filterOperator = filterOperator;
    }


    public FilterDataType getFilterDataType() {
        return filterDataType;
    }

    public void setFilterDataType(FilterDataType filterDataType) {
        this.filterDataType = filterDataType;
    }

    public boolean isExclude() {
        return exclude;
    }

    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }

    public <T> RangeFilterValue getRangeValue(Class<T> type ) {
        return (RangeFilterValue<T>) rangeValue;
    }

    public Object getMinValue() {
        return rangeValue.getMinValue();
    }

    public Object getMaxValue() {
        return rangeValue.getMaxValue();
    }

    public String getLowerBoundOperator() {
        return rangeValue.getLowerBound().getOperator();
    }

    public String getUpperBoundOperator() {
        return rangeValue.getUpperBound().getOperator();
    }

    public RangeFilterValue<String> rangeValue( ) {
        return (RangeFilterValue<String>) rangeValue;
    }

    public void setRangeValue(String min, String max) {
        Double minDouble = Double.valueOf(min);
        Double maxDouble = Double.valueOf(max);
        this.rangeValue = new RangeFilterValue<>(minDouble,maxDouble);
    }

    public void setRangeValueMax(String max) {
        Double maxDouble = Double.valueOf(max);
        if(this.rangeValue == null) {
            this.rangeValue = new RangeFilterValue<>(null,maxDouble);
        }
        this.rangeValue = new RangeFilterValue<>(this.rangeValue.getMinValue(), maxDouble);
    }

    public void setRangeValueMin(String min) {
        Double minDouble = Double.valueOf(min);
        if(this.rangeValue == null) {
            this.rangeValue = new RangeFilterValue<>(minDouble, null);
        }
        this.rangeValue = new RangeFilterValue<>(minDouble, this.rangeValue.getMaxValue());
    }

    public void setRangeValue(RangeFilterValue<?> rangeValue) {
        this.rangeValue = rangeValue;
    }

    public boolean hasRangeValue() {
        return this.rangeValue != null;
    }

    public boolean hasValue() {
        if(values != null && !values.isEmpty()) {
            return true;
        }
        if(hasRangeValue()) {
            return true;
        }
        return false;
    }

    public static SearchFilter createTermFilter(String id) {
        return createTermFilter(id, null);
    }

    public static SearchFilter createTermFilter(String id, String value) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.setId(id);
        searchFilter.setName(id);
        if(value != null) {
            searchFilter.setValues(new ArrayList<>());
            searchFilter.getValues().add(value);
        }
        searchFilter.setFilterOperator(FilterOperator.AND);
        searchFilter.setFilterType(FilterType.TERM);
        searchFilter.setFilterDataType(FilterDataType.STRING);
        return searchFilter;
    }

    @Override
    public String toString() {
        return "\nSearchFilter{" +
                "filterType=" + filterType +
                ", exclude=" + exclude +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", values=" + values +
                ", rangeFilterValue=" + rangeValue +
                ", filterOperator=" + filterOperator +
                ", filterDataType=" + filterDataType +
                "}\n";
    }
}
