package com.quasiris.qsf.query;

import com.quasiris.qsf.dto.query.ResultDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private List<BaseSearchFilter> searchFilterList = new ArrayList<>();
    private List<Facet> facetList;

    private Sort sort;

    private Integer page;

    private Integer rows;

    private boolean debug = false;

    private String requestOrigin;

    private Map<String, Object> parameters;

    private Set<String> ctrl;

    private ResultDTO result;

    public SearchQuery() {}

    public SearchQuery(SearchQuery searchQuery) {
        this.originalQuery = searchQuery.getOriginalQuery();
        this.queryChanged = searchQuery.isQueryChanged();
        if(searchQuery.getQueryChangedReasons() != null) {
            this.queryChangedReasons = new ArrayList<>(searchQuery.getQueryChangedReasons());
        }
        this.tracking = searchQuery.getTracking();
        // TODO we need a deep copy?
        if(searchQuery.getQueryToken() != null) {
            this.queryToken = new ArrayList<>(searchQuery.getQueryToken());
        }
        this.q = searchQuery.getQ();
        this.requestId = searchQuery.getRequestId();

        // TODO we need a deep copy?
        if(searchQuery.getSearchFilterList() != null) {
            this.searchFilterList = new ArrayList<>(searchQuery.getSearchFilterList());
        }

        // TODO we need a deep copy?
        if(searchQuery.getFacetList() != null) {
            this.facetList = new ArrayList<>(searchQuery.getFacetList());
        }
        this.sort = searchQuery.getSort();
        this.page = searchQuery.getPage();
        this.rows = searchQuery.getRows();
        this.debug = searchQuery.isDebug();
        this.requestOrigin = searchQuery.getRequestOrigin();

        // TODO we need a deep copy?
        if(searchQuery.getParameters() != null) {
            this.parameters = new HashMap<>(searchQuery.getParameters());
        }
    }



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

    public List<BaseSearchFilter> getSearchFilterList() {
        return searchFilterList;
    }

    public void setSearchFilterList(List<BaseSearchFilter> searchFilterList) {
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

    public void addFilter(BaseSearchFilter searchFilter) {
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

    /**
     * Getter for property 'ctrl'.
     *
     * @return Value for property 'ctrl'.
     */
    public Set<String> getCtrl() {
        return ctrl;
    }

    /**
     * Setter for property 'ctrl'.
     *
     * @param ctrl Value to set for property 'ctrl'.
     */
    public void setCtrl(Set<String> ctrl) {
        this.ctrl = ctrl;
    }

    public boolean isCtrl(String value) {
        if(this.ctrl == null) {
            return false;
        }
        return this.ctrl.contains(value);
    }

    /**
     * Getter for property 'result'.
     *
     * @return Value for property 'result'.
     */
    public ResultDTO getResult() {
        return result;
    }

    /**
     * Setter for property 'result'.
     *
     * @param result Value to set for property 'result'.
     */
    public void setResult(ResultDTO result) {
        this.result = result;
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
