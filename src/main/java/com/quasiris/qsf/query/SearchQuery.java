package com.quasiris.qsf.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Map<String, String> parameters;

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

    public void addFacet(Facet facet) {
        if(this.facetList==null) {
            facetList = new ArrayList<>();
        }
        facetList.add(facet);
    }

    public void setFacetList(List<Facet> facetList) {
        this.facetList = facetList;
    }


    public Map<String, String> getParameters() {
        return parameters;
    }

    public Map<String, String> getParametersWithPrefix(String prefix) {
        Map<String, String> prefixMap = new HashMap<>();
        if(this.parameters == null) {
            return prefixMap;
        }

        for(Map.Entry<String, String> entry : parameters.entrySet()) {
            prefixMap.put(prefix + "." + entry.getKey(), entry.getValue());
        }

        return prefixMap;
    }


    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(String key, String value) {
        if(this.parameters == null) {
            this.parameters = new HashMap<>();
        }
        this.parameters.put(key, value);
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
