package com.quasiris.qsf.config;

import com.quasiris.qsf.pipeline.filter.elastic.FacetMapping;
import com.quasiris.qsf.query.Facet;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FacetDTO {
    private  List<Facet> facets;

    private Map<String, FacetMapping> facetMapping = new LinkedHashMap<>();

    public List<Facet> getFacets() {
        return facets;
    }

    public void setFacets(List<Facet> facets) {
        this.facets = facets;
    }

    public Map<String, FacetMapping> getFacetMapping() {
        return facetMapping;
    }

    public void setFacetMapping(Map<String, FacetMapping> facetMapping) {
        this.facetMapping = facetMapping;
    }
}
