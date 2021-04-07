package com.quasiris.qsf.query;

public class Control {

    public static final String LOAD_MORE_FACETS = "loadMoreFacets";
    public static final String MODIFIED = "modified";

    public static boolean isLoadMoreFacets(SearchQuery searchQuery) {
        return searchQuery.isCtrl(LOAD_MORE_FACETS);
    }

    public static boolean isModified(SearchQuery searchQuery) {
        return searchQuery.isCtrl(MODIFIED);
    }
}
