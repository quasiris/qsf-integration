package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.config.QsfSearchConfigDTO;
import com.quasiris.qsf.config.QsfSearchConfigUtil;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.query.*;
import com.quasiris.qsf.test.json.JsonAssert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static com.quasiris.qsf.pipeline.filter.elastic.QsfqlFilterTransformerTest.mockSearchQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;

class QsfqlFilterMapperTest {

    private final String testBasePackage = "classpath://com/quasiris/qsf/test/elastic/query/expected/";


    @Test
    void createDefinedRangeFilterMultipleFilter() throws JsonBuilderException, IOException {
        // given
        List<SearchFilter> filters = new ArrayList<>();
        SearchFilter searchFilter1 = new SearchFilter();
        searchFilter1.setFilterOperator(FilterOperator.OR);
        searchFilter1.setFilterType(FilterType.DEFINED_RANGE);
        searchFilter1.setFilterDataType(FilterDataType.NUMBER);
        searchFilter1.setId("stock");
        searchFilter1.setName("stock");
        searchFilter1.setValues(Arrays.asList("In Stock", "Not in Stock"));
        filters.add(searchFilter1);


        Map<String, Range> definedFilterMapping = new HashMap<>();
        Range inStockRange = new Range("In Stock", 1, null);
        Range notnStockRange = new Range("Not in Stock", null, 1);
        definedFilterMapping.put(searchFilter1.getId() + "." + inStockRange.getValue() , inStockRange);
        definedFilterMapping.put(searchFilter1.getId() + "." + notnStockRange.getValue() , notnStockRange);

        QsfSearchConfigDTO searchConfig = QsfSearchConfigUtil.initSearchConfig();
        searchConfig.getFilter().getDefinedRangeFilterMapping().putAll(definedFilterMapping);
        // when
        QsfqlFilterMapper mapper = new QsfqlFilterMapper(searchConfig);


        JsonNode filtersOr = mapper.buildFiltersJson(filters);
        JsonAssert.assertJsonFile(testBasePackage + "defined-range-filter-query-multiple-filter.json", filtersOr);

    }
    @Test
    void createDefinedRangeFilter() throws JsonBuilderException, IOException {
        // given
        List<SearchFilter> filters = new ArrayList<>();
        SearchFilter searchFilter1 = new SearchFilter();
        searchFilter1.setFilterOperator(FilterOperator.OR);
        searchFilter1.setFilterType(FilterType.DEFINED_RANGE);
        searchFilter1.setFilterDataType(FilterDataType.NUMBER);
        searchFilter1.setId("stock");
        searchFilter1.setName("stock");
        searchFilter1.setValues(Arrays.asList("In Stock"));
        filters.add(searchFilter1);


        Map<String, Range> definedFilterMapping = new HashMap<>();
        Range inStockRange = new Range("In Stock", 1, null);
        Range notnStockRange = new Range("Not in Stock", null, 1);
        definedFilterMapping.put(searchFilter1.getId() + "." + inStockRange.getValue() , inStockRange);
        definedFilterMapping.put(searchFilter1.getId() + "." + notnStockRange.getValue() , notnStockRange);

        QsfSearchConfigDTO searchConfig = QsfSearchConfigUtil.initSearchConfig();
        searchConfig.getFilter().getDefinedRangeFilterMapping().putAll(definedFilterMapping);

        // when
        QsfqlFilterMapper mapper = new QsfqlFilterMapper(searchConfig);

        JsonNode filtersOr = mapper.buildFiltersJson(filters);
        JsonAssert.assertJsonFile(testBasePackage + "defined-range-filter-query.json", filtersOr);

    }


    @Test
    void createFilters() throws JsonBuilderException, IOException {
        // given
        List<SearchFilter> filters = new ArrayList<>();
        SearchFilter searchFilter1 = new SearchFilter();
        searchFilter1.setFilterOperator(FilterOperator.OR);
        searchFilter1.setFilterType(FilterType.TERM);
        searchFilter1.setName("brand");
        searchFilter1.setValues(Arrays.asList("samsung", "apple"));
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

        QsfSearchConfigDTO searchConfig = QsfSearchConfigUtil.initSearchConfig();

        // when
        QsfqlFilterMapper mapper = new QsfqlFilterMapper(searchConfig);
        JsonNode filtersOr = mapper.buildFiltersJson(filters);
        JsonAssert.assertJsonFile(testBasePackage + "create-filters-elastic-query.json", filtersOr);

    }

    @Test
    void buildFiltersJsonWithSearchFiltersAndEmptyBoolFilter() throws JsonBuilderException, IOException {
        // given
        SearchQuery searchQuery = mockSearchQuery();
        searchQuery.getSearchFilterList().add(new BoolSearchFilter());

        QsfSearchConfigDTO searchConfig = QsfSearchConfigUtil.initSearchConfig();

        // when
        QsfqlFilterMapper mapper = new QsfqlFilterMapper(searchConfig);
        ObjectNode node = mapper.buildFiltersJson(searchQuery.getSearchFilterList());
        JsonAssert.assertJsonFile(testBasePackage + "search-filter-with-empty-bool-filter.json", node);

    }

    @Test
    void buildFiltersJsonWithEmptyBoolFilter() throws JsonBuilderException, IOException {
        // given
        SearchQuery searchQuery = mockSearchQuery();
        searchQuery.setSearchFilterList(new ArrayList<>());
        searchQuery.getSearchFilterList().add(new BoolSearchFilter());

        QsfSearchConfigDTO searchConfig = QsfSearchConfigUtil.initSearchConfig();

        // when
        QsfqlFilterMapper mapper = new QsfqlFilterMapper(searchConfig);
        ObjectNode node = mapper.buildFiltersJson(searchQuery.getSearchFilterList());

        JsonAssert.assertJsonFile(testBasePackage + "empty-bool-filter.json", node);
    }

    @Test
    void buildFiltersJsonNullFilters() throws JsonBuilderException, IOException {
        // given
        SearchQuery searchQuery = mockSearchQuery();
        searchQuery.setSearchFilterList(null);

        QsfSearchConfigDTO searchConfig = QsfSearchConfigUtil.initSearchConfig();

        // when
        QsfqlFilterMapper mapper = new QsfqlFilterMapper(searchConfig);
        ObjectNode node = mapper.buildFiltersJson(searchQuery.getSearchFilterList());
        JsonAssert.assertJsonFile(testBasePackage + "empty-bool-filter.json", node);
    }

    @Test
    void buildFiltersJsonEmptyFilters() throws JsonBuilderException, IOException {
        // given
        SearchQuery searchQuery = mockSearchQuery();
        searchQuery.setSearchFilterList(new ArrayList<>());

        QsfSearchConfigDTO searchConfig = QsfSearchConfigUtil.initSearchConfig();

        // when
        QsfqlFilterMapper mapper = new QsfqlFilterMapper(searchConfig);
        ObjectNode node = mapper.buildFiltersJson(searchQuery.getSearchFilterList());
        JsonAssert.assertJsonFile(testBasePackage + "empty-bool-filter.json", node);
    }

    @Test
    void buildFiltersJsonSearchFilters() throws JsonBuilderException, IOException {
        // given
        SearchQuery searchQuery = mockSearchQuery();

        QsfSearchConfigDTO searchConfig = QsfSearchConfigUtil.initSearchConfig();

        // when
        QsfqlFilterMapper mapper = new QsfqlFilterMapper(searchConfig);
        ObjectNode node = mapper.buildFiltersJson(searchQuery.getSearchFilterList());
        JsonAssert.assertJsonFile(testBasePackage + "search-filter.json", node);
    }

    @Test
    void buildFiltersJsonSearchFiltersAndBoolFilters() throws JsonBuilderException, IOException {
        // given
        SearchQuery searchQuery = mockSearchQuery();

        BoolSearchFilter boolSearchFilter = new BoolSearchFilter();
        boolSearchFilter.setOperator(FilterOperator.NOT);
        boolSearchFilter.setFilters(new ArrayList<>());
        boolSearchFilter.getFilters().add(searchQuery.getSearchFilterList().get(0));
        boolSearchFilter.getFilters().add(searchQuery.getSearchFilterList().get(1));
        searchQuery.getSearchFilterList().add(boolSearchFilter);

        QsfSearchConfigDTO searchConfig = QsfSearchConfigUtil.initSearchConfig();

        // when
        QsfqlFilterMapper mapper = new QsfqlFilterMapper(searchConfig);
        ObjectNode node = mapper.buildFiltersJson(searchQuery.getSearchFilterList());
        JsonAssert.assertJsonFile(testBasePackage + "search-filter-and-bool-filters.json", node);

    }

    @Test
    void buildFiltersJsonBoolFiltersComplex() throws JsonBuilderException, IOException {
        // given
        SearchQuery searchQuery = mockSearchQuery();

        BoolSearchFilter boolSearchFilter = new BoolSearchFilter();
        boolSearchFilter.setOperator(FilterOperator.NOT);
        boolSearchFilter.setFilters(new ArrayList<>());
        boolSearchFilter.getFilters().add(searchQuery.getSearchFilterList().get(0));
        boolSearchFilter.getFilters().add(searchQuery.getSearchFilterList().get(1));
        searchQuery.getSearchFilterList().add(boolSearchFilter);

        BoolSearchFilter boolSearchFilter2 = new BoolSearchFilter();
        boolSearchFilter2.setOperator(FilterOperator.OR);
        boolSearchFilter2.setFilters(new ArrayList<>());
        boolSearchFilter2.getFilters().add(searchQuery.getSearchFilterList().get(0));

        BoolSearchFilter boolSearchFilter3 = new BoolSearchFilter();
        boolSearchFilter3.setOperator(FilterOperator.NOT);
        boolSearchFilter3.setFilters(new ArrayList<>());
        boolSearchFilter3.getFilters().add(searchQuery.getSearchFilterList().get(1));
        boolSearchFilter2.getFilters().add(boolSearchFilter3);

        searchQuery.getSearchFilterList().add(boolSearchFilter2);

        QsfSearchConfigDTO searchConfig = QsfSearchConfigUtil.initSearchConfig();

        // when
        QsfqlFilterMapper mapper = new QsfqlFilterMapper(searchConfig);
        ObjectNode node = mapper.buildFiltersJson(searchQuery.getSearchFilterList());
        JsonAssert.assertJsonFile(testBasePackage + "bool-filter-complex.json", node);


    }
}