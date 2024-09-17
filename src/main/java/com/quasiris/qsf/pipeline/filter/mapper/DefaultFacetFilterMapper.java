package com.quasiris.qsf.pipeline.filter.mapper;

import com.quasiris.qsf.commons.util.UrlUtil;
import com.quasiris.qsf.dto.response.FacetValue;

public class DefaultFacetFilterMapper extends AbstractFacetFilterMapper implements FacetFilterMapper {

    @Override
    public void map(FacetValue facetValue) {
        Object value = facetValue.getValue();
        if(getKey() != null) {
            value = getKey();
        }
        String filterValueEncoded = UrlUtil.encode(value.toString());
        String filter = getFilterPrefix() + getFacetId() + getFilterType() + "=" + getFilterValuePrefix() + filterValueEncoded;

        if(getParentFacetValue() != null) {
            filter = getParentFacetValue().getFilter().toString() + "&" + filter;

        }
        facetValue.setFilter(filter);
    }
}
