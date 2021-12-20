package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.commons.util.IOUtils;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.query.FilterDataType;
import com.quasiris.qsf.query.FilterType;
import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class QsfqlFilterTransformerTest {
    private Integer elasticVersion = 7;
    private ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, String> filterRules = new HashMap<>();
    private Map<String, String> filterMapping = new HashMap<>();
    private String filterPath = null;

    @Test
    void transformFilters() throws JsonBuilderException, IOException {
        // given
        SearchQuery searchQuery = mockSearchQuery();
        ObjectNode queryNode = mockQuery(Profiles.matchAll().replace("classpath:/", ""));
        String filterVariable = null;
        boolean multiSelectFilter = false;

        // when
        QsfqlFilterTransformer filterTransformer = new QsfqlFilterTransformer(
                elasticVersion, objectMapper,
                queryNode.deepCopy(), searchQuery,
                filterRules, filterMapping, filterPath,
                filterVariable, multiSelectFilter);
        filterTransformer.transformFilters();

        // then
        Assertions.assertEquals("{\n" +
                "  \"from\" : 0,\n" +
                "  \"size\" : 10,\n" +
                "  \"track_total_hits\" : true,\n" +
                "  \"query\" : {\n" +
                "    \"bool\" : {\n" +
                "      \"must\" : [ {\n" +
                "        \"match_all\" : { }\n" +
                "      } ],\n" +
                "      \"filter\" : {\n" +
                "        \"bool\" : {\n" +
                "          \"must\" : [ {\n" +
                "            \"bool\" : {\n" +
                "              \"must\" : [ {\n" +
                "                \"term\" : {\n" +
                "                  \"tag\" : \"wago\"\n" +
                "                }\n" +
                "              }, {\n" +
                "                \"term\" : {\n" +
                "                  \"tag\" : \"kaiser\"\n" +
                "                }\n" +
                "              } ]\n" +
                "            }\n" +
                "          }, {\n" +
                "            \"range\" : {\n" +
                "              \"price\" : {\n" +
                "                \"gte\" : 100.0,\n" +
                "                \"lte\" : 200.0\n" +
                "              }\n" +
                "            }\n" +
                "          } ]\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}", filterTransformer.getElasticQuery().toPrettyString());
    }

    @Test
    void transformFiltersWithFilterVariable() throws JsonBuilderException, IOException {
        // given
        SearchQuery searchQuery = mockSearchQuery();
        ObjectNode queryNode = mockQuery("/com/quasiris/qsf/elastic/profiles/match-all-profile-with-filtervariable.json");
        String filterVariable = "myFilter";
        boolean multiSelectFilter = false;

        // when
        QsfqlFilterTransformer filterTransformer = new QsfqlFilterTransformer(
                elasticVersion, objectMapper,
                queryNode.deepCopy(), searchQuery,
                filterRules, filterMapping, filterPath,
                filterVariable, multiSelectFilter);
        filterTransformer.transformFilters();

        // then
        Assertions.assertEquals("{\n" +
                "  \"from\" : 0,\n" +
                "  \"size\" : 10,\n" +
                "  \"track_total_hits\" : true,\n" +
                "  \"query\" : {\n" +
                "    \"bool\" : {\n" +
                "      \"must\" : [ {\n" +
                "        \"match_all\" : { }\n" +
                "      }, {\n" +
                "        \"bool\" : {\n" +
                "          \"filter\" : {\n" +
                "            \"bool\" : {\n" +
                "              \"must\" : [ {\n" +
                "                \"bool\" : {\n" +
                "                  \"must\" : [ {\n" +
                "                    \"term\" : {\n" +
                "                      \"tag\" : \"wago\"\n" +
                "                    }\n" +
                "                  }, {\n" +
                "                    \"term\" : {\n" +
                "                      \"tag\" : \"kaiser\"\n" +
                "                    }\n" +
                "                  } ]\n" +
                "                }\n" +
                "              }, {\n" +
                "                \"range\" : {\n" +
                "                  \"price\" : {\n" +
                "                    \"gte\" : 100.0,\n" +
                "                    \"lte\" : 200.0\n" +
                "                  }\n" +
                "                }\n" +
                "              } ]\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      } ]\n" +
                "    }\n" +
                "  }\n" +
                "}", filterTransformer.getElasticQuery().toPrettyString());
    }

    @Test
    void transformFiltersWithPostFilter() throws JsonBuilderException, IOException {
        // given
        SearchQuery searchQuery = mockSearchQuery();
        ObjectNode queryNode = mockQuery(Profiles.matchAll().replace("classpath:/", ""));
        String filterVariable = null;
        boolean multiSelectFilter = true;

        // when
        QsfqlFilterTransformer filterTransformer = new QsfqlFilterTransformer(
                elasticVersion, objectMapper,
                queryNode.deepCopy(), searchQuery,
                filterRules, filterMapping, filterPath,
                filterVariable, multiSelectFilter);
        filterTransformer.transformFilters();

        // then
        Assertions.assertEquals("{\n" +
                "  \"from\" : 0,\n" +
                "  \"size\" : 10,\n" +
                "  \"track_total_hits\" : true,\n" +
                "  \"query\" : {\n" +
                "    \"bool\" : {\n" +
                "      \"must\" : [ {\n" +
                "        \"match_all\" : { }\n" +
                "      } ]\n" +
                "    }\n" +
                "  },\n" +
                "  \"post_filter\" : {\n" +
                "    \"bool\" : {\n" +
                "      \"must\" : [ {\n" +
                "        \"bool\" : {\n" +
                "          \"must\" : [ {\n" +
                "            \"term\" : {\n" +
                "              \"tag\" : \"wago\"\n" +
                "            }\n" +
                "          }, {\n" +
                "            \"term\" : {\n" +
                "              \"tag\" : \"kaiser\"\n" +
                "            }\n" +
                "          } ]\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"range\" : {\n" +
                "          \"price\" : {\n" +
                "            \"gte\" : 100.0,\n" +
                "            \"lte\" : 200.0\n" +
                "          }\n" +
                "        }\n" +
                "      } ]\n" +
                "    }\n" +
                "  }\n" +
                "}", filterTransformer.getElasticQuery().toPrettyString());
    }

    @Test
    void transformFiltersWithFilterVariableAndPostFilter() throws JsonBuilderException, IOException {
        // given
        SearchQuery searchQuery = mockSearchQuery();
        ObjectNode queryNode = mockQuery("/com/quasiris/qsf/elastic/profiles/match-all-profile-with-filtervariable.json");
        String filterVariable = "myFilter";
        boolean multiSelectFilter = true;

        // when
        QsfqlFilterTransformer filterTransformer = new QsfqlFilterTransformer(
                elasticVersion, objectMapper,
                queryNode.deepCopy(), searchQuery,
                filterRules, filterMapping, filterPath,
                filterVariable, multiSelectFilter);
        filterTransformer.transformFilters();

        // then
        Assertions.assertEquals("{\n" +
                "  \"from\" : 0,\n" +
                "  \"size\" : 10,\n" +
                "  \"track_total_hits\" : true,\n" +
                "  \"query\" : {\n" +
                "    \"bool\" : {\n" +
                "      \"must\" : [ {\n" +
                "        \"match_all\" : { }\n" +
                "      }, {\n" +
                "        \"bool\" : {\n" +
                "          \"filter\" : {\n" +
                "            \"bool\" : { }\n" +
                "          }\n" +
                "        }\n" +
                "      } ]\n" +
                "    }\n" +
                "  },\n" +
                "  \"post_filter\" : {\n" +
                "    \"bool\" : {\n" +
                "      \"must\" : [ {\n" +
                "        \"bool\" : {\n" +
                "          \"must\" : [ {\n" +
                "            \"term\" : {\n" +
                "              \"tag\" : \"wago\"\n" +
                "            }\n" +
                "          }, {\n" +
                "            \"term\" : {\n" +
                "              \"tag\" : \"kaiser\"\n" +
                "            }\n" +
                "          } ]\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"range\" : {\n" +
                "          \"price\" : {\n" +
                "            \"gte\" : 100.0,\n" +
                "            \"lte\" : 200.0\n" +
                "          }\n" +
                "        }\n" +
                "      } ]\n" +
                "    }\n" +
                "  }\n" +
                "}", filterTransformer.getElasticQuery().toPrettyString());
    }

    @Test
    void transformFiltersWithMultiselectAndVariantId() {
    }

    public static SearchQuery mockSearchQuery() {
        SearchQuery searchQuery = new SearchQuery();

        SearchFilter filterTag = new SearchFilter();
        filterTag.setName("tag");
        filterTag.setFilterType(FilterType.TERM);
        filterTag.setValues(Arrays.asList("wago", "kaiser"));
        searchQuery.addFilter(filterTag);

        SearchFilter filterPrice = new SearchFilter();
        filterPrice.setName("price");
        filterPrice.setFilterType(FilterType.RANGE);
        filterPrice.setFilterDataType(FilterDataType.NUMBER);
        filterPrice.setRangeValue("100", "200");
        searchQuery.addFilter(filterPrice);

        return searchQuery;
    }

    private static ObjectNode mockQuery(String profileClasspath) throws JsonBuilderException, IOException {
        JsonBuilder jsonBuilder = new JsonBuilder();
        String matchAllQuery = IOUtils.getStringFromClassPath(profileClasspath);
        jsonBuilder.string(matchAllQuery);
        JsonNode elasticQuery = jsonBuilder.get();
        return (ObjectNode) elasticQuery;
    }

    public static String mockQueryMatchAll() {
        // TODO load from json
        return "{\"bool\":{\"must\":[{\"match_all\":{}}]}}";
    }

    public static String mockQueryMatchAllWithFilterVariable() {
        // TODO load from json
        return "{\"bool\":{\"must\":[{\"match_all\":{}}],\"filterss\":\"$filter\"}}";
    }
}