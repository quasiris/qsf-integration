package com.quasiris.qsf.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasiris.qsf.dto.query.SearchQueryDTO;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SearchQueryMapperTest {

    @Test
    void testDateRangeMaxNullFilter() throws Exception {
        SearchQueryDTO searchQueryDTO = readQueryFromFile("filter-date-range-max-null.json");
        SearchQuery searchQuery = SearchQueryMapper.map(searchQueryDTO);
        SearchFilter searchFilter = (SearchFilter) searchQuery.getSearchFilterList().get(0);

        assertNotNull(searchFilter.getMinValue());
        assertNull(searchFilter.getMaxValue());
        assertEquals(FilterType.RANGE, searchFilter.getFilterType());
        assertEquals(FilterDataType.DATE, searchFilter.getFilterDataType());
    }
    @Test
    void testDateRangeFilter() throws Exception {
        SearchQueryDTO searchQueryDTO = readQueryFromFile("filter-date-range.json");
        SearchQuery searchQuery = SearchQueryMapper.map(searchQueryDTO);
        SearchFilter searchFilter = (SearchFilter) searchQuery.getSearchFilterList().get(0);

        assertNotNull(searchFilter.getMinValue());
        assertNotNull(searchFilter.getMaxValue());
        assertEquals(FilterType.RANGE, searchFilter.getFilterType());
        assertEquals(FilterDataType.DATE, searchFilter.getFilterDataType());
    }

    @Test
    void mapOperator() throws Exception {
        SearchQueryDTO searchQueryDTO = readQueryFromFile("filter-with-operator.json");
        SearchQuery searchQuery = SearchQueryMapper.map(searchQueryDTO);
        SearchFilter searchFilter = (SearchFilter) searchQuery.getSearchFilterList().get(0);
        assertEquals(FilterOperator.NOT, searchFilter.getFilterOperator());
    }

    public SearchQueryDTO readQueryFromFile(String fileName) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String file = "src/test/resources/com/quasiris/qsf/query/" + fileName;
        SearchQueryDTO searchQueryDTO = objectMapper.readValue(new File(file), SearchQueryDTO.class);
        return searchQueryDTO;
    }
}