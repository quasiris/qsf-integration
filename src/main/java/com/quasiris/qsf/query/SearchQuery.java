package com.quasiris.qsf.query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mki on 11.11.16.
 */
public class SearchQuery {

    private List<Token> queryToken = new ArrayList<>();

    private String q;

    private String requestId;

    private List<SearchFilter> searchFilterList = new ArrayList<>();
    private List<Facet> facetList;

    private Sort sort;

    private Integer page;

    private Integer rows;

    private boolean debug = false;

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

    public void setQueryToken(List<Token> queryToken) {
        this.queryToken = queryToken;
    }

    public List<Facet> getFacetList() {
        return facetList;
    }

    public void addFacet(String id, String name) {
        if(this.facetList==null) {
            facetList = new ArrayList<>();
        }
        Facet facet = new Facet();
        facet.setId(id);
        facet.setName(name);
        facetList.add(facet);
    }

    public void setFacetList(List<Facet> facetList) {
        this.facetList = facetList;
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
