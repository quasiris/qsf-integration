package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.response.SearchResult;

/**
 * Created by mki on 11.11.17.
 */
public class SleepFilter extends AbstractFilter {


    private long sleepTime;

    public SleepFilter(String id, long sleepTime) {
        setId(id);
        this.sleepTime = sleepTime;
    }

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) {
        try {
            SearchResult searchResponse = new SearchResult();
            searchResponse.setStatusCode(200);
            searchResponse.setStatusMessage(Thread.currentThread().getName());
            Thread.sleep(sleepTime);
            pipelineContainer.putSearchResult(getId(),searchResponse);
        } catch (InterruptedException e) {
            // do nothing
        }
        return pipelineContainer;
    }
}
