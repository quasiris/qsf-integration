package com.quasiris.qsf.query;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchQueryFactoryTest {

    @Test
    void deepCopy() {
        // given
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setSort(new Sort("price", "asc"));
        List<BaseSearchFilter> filters = new ArrayList<>();
        SearchFilter rangeFilter = new SearchFilter();
        rangeFilter.setId("price");
        rangeFilter.setName("price");
        rangeFilter.setFilterType(FilterType.RANGE);
        rangeFilter.setFilterDataType(FilterDataType.NUMBER);
        rangeFilter.setRangeValue("1", "100");
        filters.add(rangeFilter);
        searchQuery.setSearchFilterList(filters);
        Facet priceFacet = new Facet();
        priceFacet.setId("priceFacet");
        priceFacet.setName("priceFacet");
        priceFacet.setOperator(FilterOperator.AND);
        searchQuery.setFacetList(Arrays.asList(priceFacet));

        // when
        SearchQuery copy = SearchQueryFactory.deepCopy(searchQuery);

        // then
        assertEquals(copy.toString(), searchQuery.toString());
    }
}