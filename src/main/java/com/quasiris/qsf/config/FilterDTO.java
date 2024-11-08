package com.quasiris.qsf.config;

import com.quasiris.qsf.query.Range;

import java.util.Map;

public class FilterDTO {
    private Map<String, String> filterRules;
    private Map<String, String> filterMapping;
    private Map<String, Range> definedRangeFilterMapping;
    private Boolean multiSelectFilter;
    private String filterVariable;
    private String filterPath;
    private String filterPrefix;

    public Map<String, Range> getDefinedRangeFilterMapping() {
        return definedRangeFilterMapping;
    }

    public void setDefinedRangeFilterMapping(Map<String, Range> definedRangeFilterMapping) {
        this.definedRangeFilterMapping = definedRangeFilterMapping;
    }

    public Map<String, String> getFilterMapping() {
        return filterMapping;
    }

    public void setFilterMapping(Map<String, String> filterMapping) {
        this.filterMapping = filterMapping;
    }

    public Map<String, String> getFilterRules() {
        return filterRules;
    }

    public void setFilterRules(Map<String, String> filterRules) {
        this.filterRules = filterRules;
    }

    public Boolean getMultiSelectFilter() {
        if(multiSelectFilter == null) {
            return false;
        }
        return multiSelectFilter;
    }

    public void setMultiSelectFilter(Boolean multiSelectFilter) {
        this.multiSelectFilter = multiSelectFilter;
    }

    public String getFilterVariable() {
        return filterVariable;
    }

    public void setFilterVariable(String filterVariable) {
        this.filterVariable = filterVariable;
    }

    public String getFilterPath() {
        return filterPath;
    }

    public void setFilterPath(String filterPath) {
        this.filterPath = filterPath;
    }

    public String getFilterPrefix() {
        if(filterPrefix == null) {
            return "";
        }
        return filterPrefix;
    }

    public void setFilterPrefix(String filterPrefix) {
        this.filterPrefix = filterPrefix;
    }
}
