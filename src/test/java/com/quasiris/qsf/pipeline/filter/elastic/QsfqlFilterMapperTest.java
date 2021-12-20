package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.query.BoolSearchFilter;
import com.quasiris.qsf.query.FilterDataType;
import com.quasiris.qsf.query.FilterOperator;
import com.quasiris.qsf.query.FilterType;
import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchQuery;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.quasiris.qsf.pipeline.filter.elastic.QsfqlFilterTransformerTest.mockSearchQuery;
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

    @Test
    void buildFiltersJsonWithSearchFiltersAndEmptyBoolFilter() throws JsonBuilderException {
        // given
        SearchQuery searchQuery = mockSearchQuery();
        searchQuery.getSearchFilterList().add(new BoolSearchFilter());

        QsfqlFilterMapper filterMapper = new QsfqlFilterMapper();

        // when
        ObjectNode node = filterMapper.buildFiltersJson(searchQuery.getSearchFilterList());

        // then
        assertEquals("{\n" +
                "  \"bool\" : {\n" +
                "    \"must\" : [ {\n" +
                "      \"bool\" : {\n" +
                "        \"must\" : [ {\n" +
                "          \"term\" : {\n" +
                "            \"tag\" : \"wago\"\n" +
                "          }\n" +
                "        }, {\n" +
                "          \"term\" : {\n" +
                "            \"tag\" : \"kaiser\"\n" +
                "          }\n" +
                "        } ]\n" +
                "      }\n" +
                "    }, {\n" +
                "      \"range\" : {\n" +
                "        \"price\" : {\n" +
                "          \"gte\" : 100.0,\n" +
                "          \"lte\" : 200.0\n" +
                "        }\n" +
                "      }\n" +
                "    } ]\n" +
                "  }\n" +
                "}", node.toPrettyString());
    }

    @Test
    void buildFiltersJsonWithEmptyBoolFilter() throws JsonBuilderException {
        // given
        SearchQuery searchQuery = mockSearchQuery();
        searchQuery.setSearchFilterList(new ArrayList<>());
        searchQuery.getSearchFilterList().add(new BoolSearchFilter());

        QsfqlFilterMapper filterMapper = new QsfqlFilterMapper();

        // when
        ObjectNode node = filterMapper.buildFiltersJson(searchQuery.getSearchFilterList());

        // then
        assertEquals("{\n" +
                "  \"bool\" : { }\n" +
                "}", node.toPrettyString());
    }

    @Test
    void buildFiltersJsonNullFilters() throws JsonBuilderException {
        // given
        SearchQuery searchQuery = mockSearchQuery();
        searchQuery.setSearchFilterList(null);

        QsfqlFilterMapper filterMapper = new QsfqlFilterMapper();

        // when
        ObjectNode node = filterMapper.buildFiltersJson(searchQuery.getSearchFilterList());

        // then
        assertEquals("{\n" +
                "  \"bool\" : { }\n" +
                "}", node.toPrettyString());
    }

    @Test
    void buildFiltersJsonEmptyFilters() throws JsonBuilderException {
        // given
        SearchQuery searchQuery = mockSearchQuery();
        searchQuery.setSearchFilterList(new ArrayList<>());

        QsfqlFilterMapper filterMapper = new QsfqlFilterMapper();

        // when
        ObjectNode node = filterMapper.buildFiltersJson(searchQuery.getSearchFilterList());

        // then
        assertEquals("{\n" +
                "  \"bool\" : { }\n" +
                "}", node.toPrettyString());
    }

    @Test
    void buildFiltersJsonSearchFilters() throws JsonBuilderException {
        // given
        SearchQuery searchQuery = mockSearchQuery();

        QsfqlFilterMapper filterMapper = new QsfqlFilterMapper();

        // when
        ObjectNode node = filterMapper.buildFiltersJson(searchQuery.getSearchFilterList());

        // then
        assertEquals("{\n" +
                "  \"bool\" : {\n" +
                "    \"must\" : [ {\n" +
                "      \"bool\" : {\n" +
                "        \"must\" : [ {\n" +
                "          \"term\" : {\n" +
                "            \"tag\" : \"wago\"\n" +
                "          }\n" +
                "        }, {\n" +
                "          \"term\" : {\n" +
                "            \"tag\" : \"kaiser\"\n" +
                "          }\n" +
                "        } ]\n" +
                "      }\n" +
                "    }, {\n" +
                "      \"range\" : {\n" +
                "        \"price\" : {\n" +
                "          \"gte\" : 100.0,\n" +
                "          \"lte\" : 200.0\n" +
                "        }\n" +
                "      }\n" +
                "    } ]\n" +
                "  }\n" +
                "}", node.toPrettyString());
    }

    @Test
    void buildFiltersJsonSearchFiltersAndBoolFilters() throws JsonBuilderException {
        // given
        SearchQuery searchQuery = mockSearchQuery();

        BoolSearchFilter boolSearchFilter = new BoolSearchFilter();
        boolSearchFilter.setOperator(FilterOperator.NOT);
        boolSearchFilter.setFilters(new ArrayList<>());
        boolSearchFilter.getFilters().add(searchQuery.getSearchFilterList().get(0));
        boolSearchFilter.getFilters().add(searchQuery.getSearchFilterList().get(1));
        searchQuery.getSearchFilterList().add(boolSearchFilter);

        QsfqlFilterMapper filterMapper = new QsfqlFilterMapper();

        // when
        ObjectNode node = filterMapper.buildFiltersJson(searchQuery.getSearchFilterList());

        // then
        assertEquals("{\n" +
                "  \"bool\" : {\n" +
                "    \"must\" : [ {\n" +
                "      \"bool\" : {\n" +
                "        \"must_not\" : [ {\n" +
                "          \"bool\" : {\n" +
                "            \"must\" : [ {\n" +
                "              \"term\" : {\n" +
                "                \"tag\" : \"wago\"\n" +
                "              }\n" +
                "            }, {\n" +
                "              \"term\" : {\n" +
                "                \"tag\" : \"kaiser\"\n" +
                "              }\n" +
                "            } ]\n" +
                "          }\n" +
                "        }, {\n" +
                "          \"range\" : {\n" +
                "            \"price\" : {\n" +
                "              \"gte\" : 100.0,\n" +
                "              \"lte\" : 200.0\n" +
                "            }\n" +
                "          }\n" +
                "        } ]\n" +
                "      }\n" +
                "    }, {\n" +
                "      \"bool\" : {\n" +
                "        \"must\" : [ {\n" +
                "          \"term\" : {\n" +
                "            \"tag\" : \"wago\"\n" +
                "          }\n" +
                "        }, {\n" +
                "          \"term\" : {\n" +
                "            \"tag\" : \"kaiser\"\n" +
                "          }\n" +
                "        } ]\n" +
                "      }\n" +
                "    }, {\n" +
                "      \"range\" : {\n" +
                "        \"price\" : {\n" +
                "          \"gte\" : 100.0,\n" +
                "          \"lte\" : 200.0\n" +
                "        }\n" +
                "      }\n" +
                "    } ]\n" +
                "  }\n" +
                "}", node.toPrettyString());
    }

    @Test
    void buildFiltersJsonBoolFiltersComplex() throws JsonBuilderException {
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

        QsfqlFilterMapper filterMapper = new QsfqlFilterMapper();

        // when
        ObjectNode node = filterMapper.buildFiltersJson(searchQuery.getSearchFilterList());

        // then
        assertEquals("{\n" +
                "  \"bool\" : {\n" +
                "    \"must\" : [ {\n" +
                "      \"bool\" : {\n" +
                "        \"must_not\" : [ {\n" +
                "          \"bool\" : {\n" +
                "            \"must\" : [ {\n" +
                "              \"term\" : {\n" +
                "                \"tag\" : \"wago\"\n" +
                "              }\n" +
                "            }, {\n" +
                "              \"term\" : {\n" +
                "                \"tag\" : \"kaiser\"\n" +
                "              }\n" +
                "            } ]\n" +
                "          }\n" +
                "        }, {\n" +
                "          \"range\" : {\n" +
                "            \"price\" : {\n" +
                "              \"gte\" : 100.0,\n" +
                "              \"lte\" : 200.0\n" +
                "            }\n" +
                "          }\n" +
                "        } ]\n" +
                "      }\n" +
                "    }, {\n" +
                "      \"bool\" : {\n" +
                "        \"should\" : [ {\n" +
                "          \"bool\" : {\n" +
                "            \"must_not\" : [ {\n" +
                "              \"range\" : {\n" +
                "                \"price\" : {\n" +
                "                  \"gte\" : 100.0,\n" +
                "                  \"lte\" : 200.0\n" +
                "                }\n" +
                "              }\n" +
                "            } ]\n" +
                "          }\n" +
                "        }, {\n" +
                "          \"bool\" : {\n" +
                "            \"must\" : [ {\n" +
                "              \"term\" : {\n" +
                "                \"tag\" : \"wago\"\n" +
                "              }\n" +
                "            }, {\n" +
                "              \"term\" : {\n" +
                "                \"tag\" : \"kaiser\"\n" +
                "              }\n" +
                "            } ]\n" +
                "          }\n" +
                "        } ]\n" +
                "      }\n" +
                "    }, {\n" +
                "      \"bool\" : {\n" +
                "        \"must\" : [ {\n" +
                "          \"term\" : {\n" +
                "            \"tag\" : \"wago\"\n" +
                "          }\n" +
                "        }, {\n" +
                "          \"term\" : {\n" +
                "            \"tag\" : \"kaiser\"\n" +
                "          }\n" +
                "        } ]\n" +
                "      }\n" +
                "    }, {\n" +
                "      \"range\" : {\n" +
                "        \"price\" : {\n" +
                "          \"gte\" : 100.0,\n" +
                "          \"lte\" : 200.0\n" +
                "        }\n" +
                "      }\n" +
                "    } ]\n" +
                "  }\n" +
                "}", node.toPrettyString());
    }
}