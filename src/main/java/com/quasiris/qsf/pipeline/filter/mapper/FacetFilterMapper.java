package com.quasiris.qsf.pipeline.filter.mapper;

import com.quasiris.qsf.dto.response.Facet;
import com.quasiris.qsf.dto.response.FacetValue;

public interface FacetFilterMapper {



    void setKey(String key);

    String getKey();
    void setFilterPrefix(String filterPrefix);

    String getFilterPrefix();

    void setFilterValuePrefix(String filterValuePrefix);

    String getFilterValuePrefix();
    void setFilterType(String filterType);
    String getFilterType();
    Facet getFacet();
    void setFacet(Facet facet);
    void map(FacetValue value);
}
