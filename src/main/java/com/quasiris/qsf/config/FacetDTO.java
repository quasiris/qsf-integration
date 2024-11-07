package com.quasiris.qsf.config;

import com.quasiris.qsf.query.Facet;

import java.util.List;

public class FacetDTO {
    private  List<Facet> facets;

    public List<Facet> getFacets() {
        return facets;
    }

    public void setFacets(List<Facet> facets) {
        this.facets = facets;
    }
}
