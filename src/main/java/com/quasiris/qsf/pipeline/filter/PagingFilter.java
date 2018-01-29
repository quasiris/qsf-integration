package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.response.Page;
import com.quasiris.qsf.response.Paging;
import com.quasiris.qsf.response.SearchResult;

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

        Paging paging = buildPaging(searchResult.getTotal(), currentPage, rows);
        searchResult.setPaging(paging);

        return pipelineContainer;

    }

    private static Paging buildPaging(Long total, Integer currentPage, Integer rows) {
        Long pageCount = ((total / rows) +1);

        Paging paging = new Paging();
        paging.setCurrentPage(currentPage);
        paging.setPageCount(pageCount.intValue());
        paging.setFirstPage(createPage(1, currentPage));
        paging.setLastPage(createPage(pageCount.intValue(), currentPage));


        paging.setNextPage(createPage(Math.min(pageCount.intValue(), (currentPage+1)), currentPage));
        paging.setPreviousPage(createPage(Math.max(1, (currentPage-1)), currentPage));

        return paging;
    }

    private static Page createPage(int number, Integer currentPage) {
        Page page = new Page();
        page.setCurrentPage(number == currentPage);
        page.setNumber(number);
        return page;
    }
}
