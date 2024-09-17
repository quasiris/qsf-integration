package com.quasiris.qsf.pipeline.filter.mapper;

import com.quasiris.qsf.dto.response.FacetValue;

public class AbstractFacetFilterMapper implements FacetFilterMapper {

    private String facetId;

    private String filterValuePrefix;
    private String filterType;
    private String filterPrefix;
    private Object key;

    private FacetValue parentFacetValue;

    @Override
    public void setKey(Object key) {
        this.key = key;
    }

    @Override
    public Object getKey() {
        return this.key;
    }

    @Override
    public void setFilterPrefix(String filterPrefix) {
        this.filterPrefix = filterPrefix;
    }

    @Override
    public String getFilterPrefix() {
        if(filterPrefix == null) {
            return "";
        }
        return filterPrefix;
    }

    @Override
    public void setFilterValuePrefix(String filterValuePrefix) {
        this.filterValuePrefix = filterValuePrefix;
    }

    @Override
    public String getFilterValuePrefix() {
        if(filterValuePrefix == null) {
            return "";
        }
        return filterValuePrefix;
    }

    @Override
    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    @Override
    public String getFilterType() {
        if(filterType == null) {
            return "";
        }
        return filterType;
    }

    @Override
    public String getFacetId() {
        return facetId;
    }

    @Override
    public void setFacetId(String facetId) {
        this.facetId = facetId;
    }

    @Override
    public void map(FacetValue value) {
    }

    public FacetValue getParentFacetValue() {
        return parentFacetValue;
    }

    @Override
    public void setParentFacetValue(FacetValue parentFacetValue) {
        this.parentFacetValue = parentFacetValue;
    }
}
