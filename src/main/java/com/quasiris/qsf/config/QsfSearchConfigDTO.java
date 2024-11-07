package com.quasiris.qsf.config;

public class QsfSearchConfigDTO {

    private Object sort;
    private Object paging;
    private Object variant;

    // profiles with conditions
    private Object profiles;

    private DisplayDTO display;
    private FacetDTO facet;
    private FilterDTO filter;


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
}
