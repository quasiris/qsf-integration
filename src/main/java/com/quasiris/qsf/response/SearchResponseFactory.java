package com.quasiris.qsf.response;

import com.quasiris.qsf.dto.response.Request;
import com.quasiris.qsf.dto.response.SearchResponse;
import com.quasiris.qsf.pipeline.PipelineContainer;

import java.util.Date;

public class SearchResponseFactory {
    public static SearchResponse create(PipelineContainer pipelineContainer, String... searchResultIds) {
        SearchResponse searchResponse = new SearchResponse();
        for(String searchResultId: searchResultIds) {
            searchResponse.getResult().put(searchResultId, pipelineContainer.getSearchResult(searchResultId));
        }

        searchResponse.setCurrentTime(new Date());
        searchResponse.setTime(pipelineContainer.currentTime());

        if(pipelineContainer.getRequest() != null) {
            searchResponse.setRequest(RequestFactory.create(pipelineContainer.getRequest()));
            if("POST".equals(searchResponse.getRequest().getMethod())) {
                searchResponse.getRequest().setQuery(pipelineContainer.getSearchQuery().getQ());
            }
        }
        searchResponse.setStatusCode(200);
        searchResponse.setRequestId(pipelineContainer.getRequestId());
        return searchResponse;
    }

    public static SearchResponse createEmpty(String q, long duration) {
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setCurrentTime(new Date());
        searchResponse.setTime(duration);

        if(q != null) {
            searchResponse.setRequest(new Request());
            searchResponse.getRequest().setQuery(q);
        }
        searchResponse.setStatusCode(200);
        return searchResponse;
    }
}
