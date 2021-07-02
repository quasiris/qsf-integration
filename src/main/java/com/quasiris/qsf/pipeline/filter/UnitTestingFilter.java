package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.dto.response.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mki on 23.12.17.
 */
public class UnitTestingFilter extends AbstractFilter {

    private static Logger LOG = LoggerFactory.getLogger(UnitTestingFilter.class);

    private String resultSetId;

    private SearchResult searchResult = new SearchResult();

    public UnitTestingFilter(String resultSetId) {
        setId(resultSetId);
        this.resultSetId = resultSetId;
    }

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) {
        pipelineContainer.putSearchResult(resultSetId, searchResult);
        return pipelineContainer;
    }

    /**
     * Getter for property 'resultSetId'.
     *
     * @return Value for property 'resultSetId'.
     */
    public String getResultSetId() {
        return resultSetId;
    }

    /**
     * Setter for property 'resultSetId'.
     *
     * @param resultSetId Value to set for property 'resultSetId'.
     */
    public void setResultSetId(String resultSetId) {
        this.resultSetId = resultSetId;
    }

    /**
     * Getter for property 'searchResult'.
     *
     * @return Value for property 'searchResult'.
     */
    public SearchResult getSearchResult() {
        return searchResult;
    }

    /**
     * Setter for property 'searchResult'.
     *
     * @param searchResult Value to set for property 'searchResult'.
     */
    public void setSearchResult(SearchResult searchResult) {
        this.searchResult = searchResult;
    }
}
