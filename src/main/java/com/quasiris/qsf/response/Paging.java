package com.quasiris.qsf.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by mki on 21.01.18.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Paging {

    private Page firstPage;

    private Page lastPage;

    private Page nextPage;
    private Page previousPage;

    private Integer pageCount;
    private Integer currentPage;

    public Page getFirstPage() {
        return firstPage;
    }

    public void setFirstPage(Page firstPage) {
        this.firstPage = firstPage;
    }

    public Page getLastPage() {
        return lastPage;
    }

    public void setLastPage(Page lastPage) {
        this.lastPage = lastPage;
    }

    public Page getNextPage() {
        return nextPage;
    }

    public void setNextPage(Page nextPage) {
        this.nextPage = nextPage;
    }

    public Page getPreviousPage() {
        return previousPage;
    }

    public void setPreviousPage(Page previousPage) {
        this.previousPage = previousPage;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    @Override
    public String toString() {
        return "Paging{" +
                "firstPage=" + firstPage +
                ", lastPage=" + lastPage +
                ", nextPage=" + nextPage +
                ", previousPage=" + previousPage +
                ", pageCount=" + pageCount +
                ", currentPage=" + currentPage +
                '}';
    }
}
