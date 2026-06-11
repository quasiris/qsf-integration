package com.quasiris.qsf.pipeline.filter.mapper;

public class BooleanFacetKeyMapper implements FacetKeyMapper {

    @Override
    public String map(Object value) {
        if (value == null) {
            return null;
        }
        String s = value.toString();
        if ("1".equals(s)) {
            return "true";
        }
        if ("0".equals(s)) {
            return "false";
        }
        return s;
    }
}
