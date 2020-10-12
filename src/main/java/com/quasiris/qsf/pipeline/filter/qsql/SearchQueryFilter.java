package com.quasiris.qsf.pipeline.filter.qsql;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.query.SearchQuery;

/**
 * Created by tbl on 12.10.20.
 *
 * The search query filter merges the defined search query with the search query in the pipeline.
 *
 * The defined values overwrite the values of the runtime values.
 * In case of lists, the values are added.
 */
public class SearchQueryFilter extends AbstractFilter {

    private SearchQuery definedSearchQuery;


    public SearchQueryFilter(SearchQuery searchQuery) {
        this.definedSearchQuery = searchQuery;
    }


    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        SearchQuery searchQuery = pipelineContainer.getSearchQuery();
        SearchQuery merged = merge(searchQuery, definedSearchQuery);

        pipelineContainer.setSearchQuery(merged);
        return pipelineContainer;
    }


    public SearchQuery merge(SearchQuery left, SearchQuery right) {
        left.getSearchFilterList().addAll(right.getSearchFilterList());

        if(left.getFacetList() != null && right.getFacetList() != null) {
            left.getFacetList().addAll(right.getFacetList());
        } else if(left.getFacetList() == null) {
            left.setFacetList(right.getFacetList());
        }

        if(left.getParameters() != null && right.getParameters() != null) {
            left.getParameters().putAll(right.getParameters());
        } else if(left.getParameters() == null) {
            left.setParameters(right.getParameters());
        }

        if(left.getSort() == null && right.getSort() != null) {
            left.setSort(right.getSort());
        }

        if(right.getRows() != null) {
            left.setRows(right.getRows());
        }

        if(right.getTracking() != null) {
            left.setTracking(right.getTracking());
        }

        return left;
    }


}
