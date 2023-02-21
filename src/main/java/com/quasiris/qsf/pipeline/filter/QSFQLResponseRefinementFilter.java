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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        refineFacets(searchResult);

        refineSorts(searchResult, searchQuery);

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

    public void refineSorts(SearchResult searchResult, SearchQuery searchQuery) {
        if(searchResult.getSort() == null) {
            return;
        }

        if(searchResult.getSort().getSort() == null) {
            return;
        }

        if(searchQuery.getSort().getField() != null) {
            for(SortEntry sortEntry : searchResult.getSort().getSort()) {
                if(sortEntry.getId().equals(searchQuery.getSort().getField())) {
                    sortEntry.setSelected(Boolean.TRUE);
                }
            }
        }
    }
    public void refineFacets(SearchResult searchResult) {
        adjustMinRangeMaxRangeForSliders(searchResult.getFacets());
        List<Facet> refinedFacets = removeFacetsWithoutValues(searchResult.getFacets());
        searchResult.setFacets(refinedFacets);
    }

    private void adjustMinRangeMaxRangeForSliders(List<Facet> facets) {
        if(facets == null) {
            return;
        }
        for(Facet facet : facets) {
            if("slider".equals(facet.getType())) {
                if(facet.getMinValue() != null && facet.getMinRange() != null &&
                        facet.getMinValue() < facet.getMinRange()) {
                    facet.setMinRange(facet.getMinValue());
                }
                if(facet.getMaxValue() != null && facet.getMaxRange() != null &&
                        facet.getMaxValue() > facet.getMaxRange()) {
                    facet.setMaxRange(facet.getMaxValue());
                }
            }
        }
    }

    private List<Facet> removeFacetsWithoutValues(List<Facet> facets) {
        if(facets == null) {
            return null;
        }
        List<Facet> refinedFacets = new ArrayList<>();
        for(Facet facet : facets) {
            if("slider".equals(facet.getType())) {
                refinedFacets.add(facet);
            } else if(facet.getValues() != null && facet.getValues().size() > 0) {
                refinedFacets.add(facet);
            }
        }
        return refinedFacets;

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
