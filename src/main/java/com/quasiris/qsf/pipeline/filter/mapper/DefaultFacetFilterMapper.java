package com.quasiris.qsf.pipeline.filter.mapper;

import com.quasiris.qsf.commons.util.UrlUtil;
import com.quasiris.qsf.dto.response.FacetValue;

public class DefaultFacetFilterMapper extends AbstractFacetFilterMapper implements FacetFilterMapper {

    @Override
    public void map(FacetValue value) {
        String filterValueEncoded = UrlUtil.encode(getKey());
        value.setFilter(getFilterPrefix() + getFacet().getId() + getFilterType() + "=" + getFilterValuePrefix() + filterValueEncoded);
    }
}
