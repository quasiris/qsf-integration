package com.quasiris.qsf.pipeline.filter.mapper;

import com.quasiris.qsf.commons.util.UrlUtil;
import com.quasiris.qsf.dto.response.FacetValue;

public class NavigationValueFacetFilterMapper extends AbstractFacetFilterMapper implements FacetFilterMapper {

    @Override
    public void map(FacetValue facetValue) {
        String filterValueEncoded = UrlUtil.encode(facetValue.getFilter().toString());
        String filter = getFilterPrefix() + getFacetId() + getFilterType() + "=" + getFilterValuePrefix() + filterValueEncoded;
        facetValue.setFilter(filter);
    }
}
