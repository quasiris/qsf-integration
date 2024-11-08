package com.quasiris.qsf.config;

import java.util.Set;

public class QsfSearchConfigDTO {




    // profiles with conditions
    private Object profiles;

    private DisplayDTO display;
    private FacetDTO facet;
    private FilterDTO filter;
    private SortDTO sort;
    private PagingDTO paging;
    private VariantDTO variant;


    public DisplayDTO getDisplay() {
        return display;
    }

    public void setDisplay(DisplayDTO display) {
        this.display = display;
    }

    public FacetDTO getFacet() {
        return facet;
    }

    public void setFacet(FacetDTO facet) {
        this.facet = facet;
    }

    public FilterDTO getFilter() {
        return filter;
    }

    public void setFilter(FilterDTO filter) {
        this.filter = filter;
    }

    public SortDTO getSort() {
        return sort;
    }

    public void setSort(SortDTO sort) {
        this.sort = sort;
    }

    public PagingDTO getPaging() {
        return paging;
    }

    public void setPaging(PagingDTO paging) {
        this.paging = paging;
    }

    public VariantDTO getVariant() {
        return variant;
    }

    public void setVariant(VariantDTO variant) {
        this.variant = variant;
    }
}
