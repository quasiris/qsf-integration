package com.quasiris.qsf.query;

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

    public RangeFilterValue<String> rangeValue( ) {
        return (RangeFilterValue<String>) rangeValue;
    }

    public void setRangeValue(String min, String max) {
        this.rangeValue = new RangeFilterValue<>(min,max);
    }

    public void setRangeValue(RangeFilterValue<?> rangeValue) {
        this.rangeValue = rangeValue;
    }


    @Override
    public String toString() {
        return "SearchFilter{" +
                "filterType=" + filterType +
                ", exclude=" + exclude +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", values=" + values +
                ", rangeFilterValue=" + rangeValue +
                '}';
    }
}
