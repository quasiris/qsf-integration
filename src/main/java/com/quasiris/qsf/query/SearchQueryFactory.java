package com.quasiris.qsf.query;

import com.quasiris.qsf.util.SerializationUtils;

import java.util.HashMap;

public class SearchQueryFactory {
    /**
     * Create a copy of the searchQuery.
     * @param searchQuery to copy from
     * @return copy of search query
     */
    public static SearchQuery deepCopy(SearchQuery searchQuery) {
        SearchQuery searchQueryCopy = new SearchQuery();
        searchQueryCopy.setOriginalQuery(searchQuery.getOriginalQuery());
        searchQueryCopy.setExplain(searchQuery.isExplain());
        searchQueryCopy.setQueryChanged(searchQuery.isQueryChanged());
        if(searchQuery.getQueryChangedReasons() != null) {
            searchQueryCopy.setQueryChangedReasons(SerializationUtils.deepCopyList(searchQuery.getQueryChangedReasons()));
        }
        searchQueryCopy.setTracking(searchQuery.getTracking());
        if(searchQuery.getQueryToken() != null) {
            searchQueryCopy.setQueryToken(SerializationUtils.deepCopyList(searchQuery.getQueryToken()));
        }
        searchQueryCopy.setQ(searchQuery.getQ());
        searchQueryCopy.setRequestId(searchQuery.getRequestId());

        if(searchQuery.getSearchFilterList() != null) {
            searchQueryCopy.setSearchFilterList(SerializationUtils.deepCopyList(searchQuery.getSearchFilterList()));
        }

        if(searchQuery.getFacetList() != null) {
            searchQueryCopy.setFacetList(SerializationUtils.deepCopyList(searchQuery.getFacetList()));
        }
        searchQueryCopy.setSort(searchQuery.getSort());
        searchQueryCopy.setPage(searchQuery.getPage());
        searchQueryCopy.setRows(searchQuery.getRows());
        searchQueryCopy.setDebug(searchQuery.isDebug());
        searchQueryCopy.setRequestOrigin(searchQuery.getRequestOrigin());

        // TODO implement deep copy
        if(searchQuery.getParameters() != null) {
            searchQueryCopy.setParameters(new HashMap<>(searchQuery.getParameters()));
        }

        return searchQueryCopy;
    }
}
