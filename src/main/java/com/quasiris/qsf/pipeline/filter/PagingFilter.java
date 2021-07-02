package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.paging.PagingBuilder;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.dto.response.Page;
import com.quasiris.qsf.dto.response.Paging;
import com.quasiris.qsf.dto.response.SearchResult;

/**
 * Created by mki on 21.01.18.
 */
public class PagingFilter extends AbstractFilter {


    private String resultId;

    public PagingFilter(String resultId) {
        this.resultId = resultId;
    }

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) {
        SearchResult searchResult = pipelineContainer.getSearchResult(resultId);
        Integer currentPage = pipelineContainer.getSearchQuery().getPage();
        Integer rows = pipelineContainer.getSearchQuery().getRows();

        if(searchResult == null) {
            return pipelineContainer;
        }

        if(currentPage == null) {
            currentPage = 1;
        }

        if(rows==null) {
            rows = 10;
        }

        Paging paging = PagingBuilder.buildPaging(searchResult.getTotal(), currentPage, rows);
        searchResult.setPaging(paging);

        return pipelineContainer;

    }
}
