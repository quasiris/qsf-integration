package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.query.FilterDataType;
import com.quasiris.qsf.query.FilterOperator;
import com.quasiris.qsf.query.FilterType;
import com.quasiris.qsf.query.SearchFilter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QsfqlFilterMapperTest {

    @Test
    void createFilters() throws JsonBuilderException {
        // given
        List<SearchFilter> filters = new ArrayList<>();
        SearchFilter searchFilter1 = new SearchFilter();
        searchFilter1.setFilterOperator(FilterOperator.OR);
        searchFilter1.setFilterType(FilterType.TERM);
        searchFilter1.setName("brand");
        searchFilter1.setValues(Arrays.asList("samsung"));
        filters.add(searchFilter1);
        SearchFilter searchFilter2 = new SearchFilter();
        searchFilter2.setFilterOperator(FilterOperator.OR);
        searchFilter2.setFilterType(FilterType.TERM);
        searchFilter2.setName("tags");
        searchFilter2.setValues(Arrays.asList("sale"));
        filters.add(searchFilter2);
        SearchFilter searchFilter3 = new SearchFilter();
        searchFilter3.setFilterOperator(FilterOperator.AND);
        searchFilter3.setFilterType(FilterType.RANGE);
        searchFilter3.setFilterDataType(FilterDataType.NUMBER);
        searchFilter3.setName("price");
        searchFilter3.setRangeValue("100", "200");
        filters.add(searchFilter3);
        SearchFilter searchFilter4 = new SearchFilter();
        searchFilter4.setFilterOperator(FilterOperator.NOT);
        searchFilter4.setFilterType(FilterType.TERM);
        searchFilter4.setName("tags");
        searchFilter4.setValues(Arrays.asList("new"));
        filters.add(searchFilter4);

        // when
        JsonNode filtersOr = QsfqlFilterMapper.createFilters(filters);

        // then
        assertEquals("{\"bool\":{\"must\":[{\"range\":{\"price\":{\"gte\":100.0,\"lte\":200.0}}}],\"must_not\":{\"bool\":{\"should\":[{\"term\":{\"tags\":\"new\"}}]}},\"should\":[{\"term\":{\"brand\":\"samsung\"}},{\"term\":{\"tags\":\"sale\"}}]}}", filtersOr.toString());
    }
}