package com.quasiris.qsf.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.quasiris.qsf.pipeline.PipelineContainer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mki on 11.11.16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponse {

    private Integer statusCode = 200;

    private Map<String, SearchResult> result = new HashMap<>();

    private long time;

    private Date currentTime = new Date();

    private Request request;

    private String requestId;

    private DidYouMeanResult didYouMeanResult;

    /**
     * Getter for property 'didYouMeanResult'.
     *
     * @return Value for property 'didYouMeanResult'.
     */
    public DidYouMeanResult getDidYouMeanResult() {
        return didYouMeanResult;
    }

    /**
     * Setter for property 'didYouMeanResult'.
     *
     * @param didYouMeanResult Value to set for property 'didYouMeanResult'.
     */
    public void setDidYouMeanResult(DidYouMeanResult didYouMeanResult) {
        this.didYouMeanResult = didYouMeanResult;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, SearchResult> getResult() {
        return result;
    }

    public void setResult(Map<String, SearchResult> result) {
        this.result = result;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "GMT")
    public Date getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    /**
     * Getter for property 'requestId'.
     *
     * @return Value for property 'requestId'.
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Setter for property 'requestId'.
     *
     * @param requestId Value to set for property 'requestId'.
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public static SearchResponse create(PipelineContainer pipelineContainer, String... searchResultIds) {
        SearchResponse searchResponse = new SearchResponse();
        for(String searchResultId: searchResultIds) {
            searchResponse.getResult().put(searchResultId, pipelineContainer.getSearchResult(searchResultId));
        }

        searchResponse.setCurrentTime(new Date());
        searchResponse.setTime(pipelineContainer.currentTime());

        if(pipelineContainer.getRequest() != null) {
            searchResponse.setRequest(new Request(pipelineContainer.getRequest()));
            if("POST".equals(searchResponse.getRequest().getMethod())) {
                searchResponse.getRequest().setQuery(pipelineContainer.getSearchQuery().getQ());
            }
        }



        searchResponse.setStatusCode(200);
        searchResponse.setRequestId(pipelineContainer.getRequestId());
        return searchResponse;

    }
}
