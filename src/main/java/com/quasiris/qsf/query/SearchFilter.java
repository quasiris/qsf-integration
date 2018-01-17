package com.quasiris.qsf.query;

import java.util.List;

/**
 * Created by mki on 11.11.16.
 */
public class SearchFilter {

    private Operator operator = Operator.OR;

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

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
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
                "operator=" + operator +
                ", exclude=" + exclude +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", values=" + values +
                ", rangeFilterValue=" + rangeValue +
                '}';
    }
}
