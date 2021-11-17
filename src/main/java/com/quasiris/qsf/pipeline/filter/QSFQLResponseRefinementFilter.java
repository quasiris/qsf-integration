package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.paging.PagingBuilder;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.exception.PipelineConfigException;
import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.dto.response.*;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by mki on 21.01.18.
 */
public class QSFQLResponseRefinementFilter extends AbstractFilter {


    private String resultId;

    public QSFQLResponseRefinementFilter() {
    }

    public QSFQLResponseRefinementFilter(String resultId) {
        this.resultId = resultId;
    }

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) {
        if(StringUtils.isEmpty(resultId)) {
            throw new PipelineConfigException(pipelineContainer, "Required parameter resultId MUST NOT be empty!");
        }
        SearchResult searchResult = pipelineContainer.getSearchResult(resultId);
        SearchQuery searchQuery = pipelineContainer.getSearchQuery();

        // filter, facets, sliders
        for(SearchFilter searchFilter : searchQuery.getSearchFilterList()) {
            String filterId = searchFilter.getId();
            Facet facet = searchResult.getFacetById(filterId);
            if(facet != null) {
                // TODO refine sliders
                facet.setSelected(Boolean.TRUE);
                if(searchFilter.getValues() != null) {
                    for (String filterValue : searchFilter.getValues()) {
                        FacetValue facetValue = facet.getFacetValueByValue(filterValue);
                        if (facetValue != null) {
                            facetValue.setSelected(Boolean.TRUE);
                        }
                    }
                }

            }
        }


        // paging
        if(checkPagingEnabled(searchQuery)) {
            Paging paging = PagingBuilder.buildPaging(searchResult.getTotal(), searchQuery.getPage(), searchQuery.getRows());
            searchResult.setPaging(paging);
        }

        return pipelineContainer;
    }

    public boolean checkPagingEnabled(SearchQuery searchQuery) {
        if(searchQuery.getResult() != null &&
                searchQuery.getResult().getPaging() != null &&
                searchQuery.getResult().getPaging().getEnabled() != null) {
            return searchQuery.getResult().getPaging().getEnabled();
        }
        return true;
    }

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }
}
