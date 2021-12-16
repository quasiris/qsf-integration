package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.paging.PagingBuilder;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.exception.PipelineConfigException;
import com.quasiris.qsf.query.BaseSearchFilter;
import com.quasiris.qsf.query.BoolSearchFilter;
import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.dto.response.*;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

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
        refineFilters(searchResult, searchQuery.getSearchFilterList());

        // paging
        if(checkPagingEnabled(searchQuery)) {
            Paging paging = PagingBuilder.buildPaging(searchResult.getTotal(), searchQuery.getPage(), searchQuery.getRows());
            searchResult.setPaging(paging);
        }

        // position on documents
        computePositions(searchQuery, searchResult);

        return pipelineContainer;
    }

    public void refineFilters(SearchResult searchResult, List<BaseSearchFilter> searchFilters) {
        for(BaseSearchFilter baseSearchFilter : searchFilters) {
            if (baseSearchFilter instanceof BoolSearchFilter) {
                BoolSearchFilter searchFilter = (BoolSearchFilter) baseSearchFilter;
                refineFilters(searchResult, searchFilter.getFilters());
            } else if(baseSearchFilter instanceof SearchFilter) {
                SearchFilter searchFilter = (SearchFilter) baseSearchFilter;
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
                    } else if (searchFilter.getMinValue() != null && searchFilter.getRangeValue(Double.class).getMinValue() instanceof Double) {
                        facet.setMinValue((Double) searchFilter.getRangeValue(Double.class).getMinValue());
                        facet.setMaxValue((Double) searchFilter.getRangeValue(Double.class).getMaxValue());

                    }
                }
            }
        }
    }

    public void computePositions(SearchQuery searchQuery, SearchResult searchResult) {
        long count = 1;
        long currentPage = 1;
        if(searchQuery.getPage() != null) {
            currentPage = searchQuery.getPage();
        }

        if(currentPage < 1) {
            currentPage = 1;
        }

        long rows = 10;
        if(searchQuery.getRows() != null) {
            rows = searchQuery.getRows();
        }

        long offset = (currentPage -1) * rows;
        for(Document document : searchResult.getDocuments()) {
            document.setPosition(offset + count);
            count++;
        }
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
