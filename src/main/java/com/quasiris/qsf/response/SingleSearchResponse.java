package com.quasiris.qsf.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

/**
 * Created by mki on 03.12.17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SingleSearchResponse {


    private Integer statusCode;
    private SimpleSearchResponse searchresult;

    private long time;

    private Date currentTime;

    private Long total;


    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public SimpleSearchResponse getSearchresult() {
        return searchresult;
    }

    public void setSearchresult(SimpleSearchResponse searchresult) {
        this.searchresult = searchresult;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Date getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public static SingleSearchResponse create(SearchResult searchResult) {
        SingleSearchResponse singleSearchResponse = new SingleSearchResponse();

        singleSearchResponse.setCurrentTime(new Date());
        singleSearchResponse.setStatusCode(searchResult.getStatusCode());
        singleSearchResponse.setTime(searchResult.getTime());

        SimpleSearchResponse searchResultResponse = new SimpleSearchResponse();
        if(searchResult == null) {
            singleSearchResponse.setTotal(0L);
            return singleSearchResponse;
        }
        if(searchResult.getDocuments() == null) {
            singleSearchResponse.setTotal(0L);
            return singleSearchResponse;
        }

        for(Document document : searchResult.getDocuments()) {
            searchResultResponse.add(document.getDocument());
        }

        singleSearchResponse.setTotal(searchResult.getTotal());
        return singleSearchResponse;
    }
}
