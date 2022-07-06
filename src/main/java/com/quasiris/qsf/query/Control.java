package com.quasiris.qsf.query;

public class Control {

    public static final String LOAD_MORE_FACETS = "loadMoreFacets";
    public static final String MODIFIED = "modified";

    public static final String NO_CAHCE = "noCache";

    public static final String SPELLCHECK_DISABLED = "spellcheckDisabled";

    public static boolean isLoadMoreFacets(SearchQuery searchQuery) {
        return searchQuery.isCtrl(LOAD_MORE_FACETS);
    }

    public static boolean isModified(SearchQuery searchQuery) {
        return searchQuery.isCtrl(MODIFIED);
    }

    public static boolean isNoCache(SearchQuery searchQuery) {
        return searchQuery.isCtrl(NO_CAHCE);
    }

    public static boolean isSpellcheckDisabled(SearchQuery searchQuery) {
        return searchQuery.isCtrl(SPELLCHECK_DISABLED);
    }
}
