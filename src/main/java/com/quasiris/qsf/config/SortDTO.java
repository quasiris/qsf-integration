package com.quasiris.qsf.config;

import java.util.HashMap;
import java.util.Map;

public class SortDTO {
    private Map<String, String> sortMapping = new HashMap<>();
    private String defaultSort;

    private Map<String, String> sortRules = new HashMap<>();

    public String getDefaultSort() {
        return defaultSort;
    }

    public void setDefaultSort(String defaultSort) {
        this.defaultSort = defaultSort;
    }

    public Map<String, String> getSortMapping() {
        return sortMapping;
    }

    public void setSortMapping(Map<String, String> sortMapping) {
        this.sortMapping = sortMapping;
    }

    public Map<String, String> getSortRules() {
        return sortRules;
    }

    public void setSortRules(Map<String, String> sortRules) {
        this.sortRules = sortRules;
    }
}
