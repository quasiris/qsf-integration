package com.quasiris.qsf.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mki on 11.11.16.
 */
public class SearchQuery {

    private String originalQuery;
    private boolean queryChanged = false;
    private List<String> queryChangedReasons;
    private Boolean tracking;

    private List<Token> queryToken = new ArrayList<>();

    private String q;

    private String requestId;

    private List<SearchFilter> searchFilterList = new ArrayList<>();
    private List<Facet> facetList;

    private Sort sort;

    private Integer page;

    private Integer rows;

    private boolean debug = false;

    private String requestOrigin;

    private Map<String, Object> parameters;

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public List<SearchFilter> getSearchFilterList() {
        return searchFilterList;
    }

    public void setSearchFilterList(List<SearchFilter> searchFilterList) {
        this.searchFilterList = searchFilterList;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public List<Token> getQueryToken() {
        return queryToken;
    }

    public List<Token> copyQueryToken() {
        List<Token> copy = new ArrayList<>();
        for(Token token: getQueryToken()) {
            Token newToken = new Token(token);
            copy.add(newToken);
        }
        return copy;
    }



    public void setQueryToken(List<Token> queryToken) {
        this.queryToken = queryToken;
    }

    public List<Facet> getFacetList() {
        return facetList;
    }

    public void addFacet(String id, String name) {
        Facet facet = new Facet();
        facet.setId(id);
        facet.setName(name);
        addFacet(facet);
    }

    public void addFilter(String id, String value) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.setId(id);
        searchFilter.setName(id);
        searchFilter.addValue(value);
        searchFilter.setFilterType(FilterType.MATCH);
        addFilter(searchFilter);
    }

    public void addFilter(SearchFilter searchFilter) {
        if(this.searchFilterList == null) {
            this.searchFilterList = new ArrayList<>();
        }
        this.searchFilterList.add(searchFilter);
    }

    public void addFacet(Facet facet) {
        if(this.facetList==null) {
            facetList = new ArrayList<>();
        }
        facetList.add(facet);
    }

    public void setFacetList(List<Facet> facetList) {
        this.facetList = facetList;
    }


    public Map<String, Object> getParameters() {
        return parameters;
    }

    public Map<String, Object> getParametersWithPrefix(String prefix) {
        Map<String, Object> prefixMap = new HashMap<>();
        if(this.parameters == null) {
            return prefixMap;
        }

        for(Map.Entry<String, Object> entry : parameters.entrySet()) {
            prefixMap.put(prefix + "." + entry.getKey(), entry.getValue());
        }

        return prefixMap;
    }


    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(String key, Object value) {
        if(this.parameters == null) {
            this.parameters = new HashMap<>();
        }
        this.parameters.put(key, value);
    }



    /**
     * Getter for property 'tracking'.
     *
     * @return Value for property 'tracking'.
     */
    public Boolean getTracking() {
        return tracking;
    }

    /**
     * Setter for property 'tracking'.
     *
     * @param tracking Value to set for property 'tracking'.
     */
    public void setTracking(Boolean tracking) {
        this.tracking = tracking;
    }


    /**
     * Getter for property 'requestOrigin'.
     *
     * @return Value for property 'requestOrigin'.
     */
    public String getRequestOrigin() {
        return requestOrigin;
    }

    /**
     * Setter for property 'requestOrigin'.
     *
     * @param requestOrigin Value to set for property 'requestOrigin'.
     */
    public void setRequestOrigin(String requestOrigin) {
        this.requestOrigin = requestOrigin;
    }

    /**
     * Getter for property 'originalQuery'.
     *
     * @return Value for property 'originalQuery'.
     */
    public String getOriginalQuery() {
        return originalQuery;
    }

    /**
     * Setter for property 'originalQuery'.
     *
     * @param originalQuery Value to set for property 'originalQuery'.
     */
    public void setOriginalQuery(String originalQuery) {
        this.originalQuery = originalQuery;
    }

    /**
     * Getter for property 'queryChanged'.
     *
     * @return Value for property 'queryChanged'.
     */
    public boolean isQueryChanged() {
        return queryChanged;
    }

    /**
     * Setter for property 'queryChanged'.
     *
     * @param queryChanged Value to set for property 'queryChanged'.
     */
    public void setQueryChanged(boolean queryChanged) {
        this.queryChanged = queryChanged;
    }

    /**
     * Getter for property 'queryChangedReasons'.
     *
     * @return Value for property 'queryChangedReasons'.
     */
    public List<String> getQueryChangedReasons() {
        return queryChangedReasons;
    }

    /**
     * Setter for property 'queryChangedReasons'.
     *
     * @param queryChangedReasons Value to set for property 'queryChangedReasons'.
     */
    public void setQueryChangedReasons(List<String> queryChangedReasons) {
        this.queryChangedReasons = queryChangedReasons;
    }

    public void addQueryChangedReason(String reason) {
        if(this.queryChangedReasons == null) {
            this.queryChangedReasons = new ArrayList<>();
        }
        this.queryChangedReasons.add(reason);
    }

    @Override
    public String toString() {
        return "SearchQuery{" +
                "queryToken=" + queryToken +
                ", q='" + q + '\'' +
                ", requestId='" + requestId + '\'' +
                ", searchFilterList=" + searchFilterList +
                ", facetList=" + facetList +
                ", sort=" + sort +
                ", page=" + page +
                ", rows=" + rows +
                ", debug=" + debug +
                '}';
    }
}
