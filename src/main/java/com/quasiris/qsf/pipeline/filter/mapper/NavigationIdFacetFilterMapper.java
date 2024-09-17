package com.quasiris.qsf.pipeline.filter.mapper;

import com.quasiris.qsf.dto.response.FacetValue;

public class NavigationIdFacetFilterMapper extends AbstractFacetFilterMapper implements FacetFilterMapper {

    @Override
    public void map(FacetValue facetValue) {
        if(facetValue.getCustomData() != null) {
            facetValue.setFilter(facetValue.getCustomData().get("id"));
            facetValue.setCustomData(null);
        }
    }
}
