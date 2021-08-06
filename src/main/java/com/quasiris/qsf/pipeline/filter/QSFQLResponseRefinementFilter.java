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
            Slider slider = searchResult.getSliderById(filterId);
            if(slider != null) {
                slider.setMinValueOrDefault((Double) searchFilter.getRangeValue(Double.class).getMinValue());
                slider.setMaxValueOrDefault((Double) searchFilter.getRangeValue(Double.class).getMaxValue());
                slider.setSelected(Boolean.TRUE);
            }

            Facet facet = searchResult.getFacetById(filterId);
            if(facet != null) {
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
        Paging paging = PagingBuilder.buildPaging(searchResult.getTotal(), searchQuery.getPage(), searchQuery.getRows());
        searchResult.setPaging(paging);


        return pipelineContainer;
    }

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }
}
