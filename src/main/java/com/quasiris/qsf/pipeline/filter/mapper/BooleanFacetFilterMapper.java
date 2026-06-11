package com.quasiris.qsf.pipeline.filter.mapper;

import com.quasiris.qsf.dto.response.FacetValue;

public class BooleanFacetFilterMapper extends DefaultFacetFilterMapper {

    @Override
    public void map(FacetValue facetValue) {
        Object key = getKey();
        if ("1".equals(String.valueOf(key))) {
            setKey("true");
        } else if ("0".equals(String.valueOf(key))) {
            setKey("false");
        }
        super.map(facetValue);
    }
}
