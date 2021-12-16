package com.quasiris.qsf.query;

import java.util.List;

public class BoolSearchFilter extends BaseSearchFilter {
    private List<BaseSearchFilter> filters;
    private FilterOperator operator;

    public List<BaseSearchFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<BaseSearchFilter> filters) {
        this.filters = filters;
    }

    public FilterOperator getOperator() {
        return operator;
    }

    public void setOperator(FilterOperator operator) {
        this.operator = operator;
    }

    @Override
    public String getFilterClass() {
        return "bool";
    }
}
