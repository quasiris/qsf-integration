package com.quasiris.qsf.query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mki on 11.11.16.
 */
public class SearchQuery {

    private String q;

    private String requestId;

    private List<SearchFilter> searchFilterList = new ArrayList<>();

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
}
