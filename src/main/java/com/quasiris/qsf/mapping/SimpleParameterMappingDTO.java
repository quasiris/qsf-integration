package com.quasiris.qsf.mapping;

import java.util.Map;

public class SimpleParameterMappingDTO {

    private Map<String, String> mapping;

    public Map<String, String> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, String> mapping) {
        this.mapping = mapping;
    }

    @Override
    public String toString() {
        return "MappingDTO{" +
                "mapping=" + mapping +
                '}';
    }
}

