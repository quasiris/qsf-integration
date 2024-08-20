package com.quasiris.qsf.pipeline.filter.mapper;

import com.quasiris.qsf.dto.response.Facet;
import com.quasiris.qsf.dto.response.FacetValue;

public interface FacetFilterMapper {



    void setKey(Object key);

    Object getKey();
    void setFilterPrefix(String filterPrefix);

    String getFilterPrefix();

    void setFilterValuePrefix(String filterValuePrefix);

    String getFilterValuePrefix();
    void setFilterType(String filterType);
    String getFilterType();
    Facet getFacet();
    void setFacet(Facet facet);
    void map(FacetValue value);

    void setParentFacetValue(FacetValue parentFacetValue);

    FacetValue getParentFacetValue();

}
