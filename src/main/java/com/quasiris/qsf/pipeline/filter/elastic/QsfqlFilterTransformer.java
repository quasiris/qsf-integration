package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.query.FilterOperator;
import com.quasiris.qsf.query.RangeFilterValue;
import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchQuery;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QsfqlFilterTransformer {


    private Integer elasticVersion = 6;

    private ObjectMapper objectMapper;

    private ObjectNode elasticQuery;

    private SearchQuery searchQuery;

    private Map<String, String> filterRules = new HashMap<>();

    private Map<String, String> filterMapping = new HashMap<>();


    public QsfqlFilterTransformer(ObjectMapper objectMapper, ObjectNode elasticQuery, SearchQuery searchQuery) {
        this.objectMapper = objectMapper;
        this.elasticQuery = elasticQuery;
        this.searchQuery = searchQuery;
    }


    public QsfqlFilterTransformer(Integer elasticVersion, ObjectMapper objectMapper, ObjectNode elasticQuery, SearchQuery searchQuery, Map<String, String> filterRules, Map<String, String> filterMapping) {
        this.elasticVersion = elasticVersion;
        this.objectMapper = objectMapper;
        this.elasticQuery = elasticQuery;
        this.searchQuery = searchQuery;
        this.filterRules = filterRules;
        this.filterMapping = filterMapping;
    }

    public ObjectNode getElasticQuery() {
        return elasticQuery;
    }


    public Map<String, String> getFilterRules() {
        return filterRules;
    }


    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }


    private SearchQuery getSearchQuery() {
        return searchQuery;
    }

    public Map<String, String> getFilterMapping() {
        return filterMapping;
    }




    public void transformFilters() throws JsonBuilderException {
        if(elasticVersion < 2) {
            transformFiltersVersionOlder2();
            return;
        }
        transformFiltersCurrentVersion();
    }

    public void transformFiltersVersionOlder2() throws JsonBuilderException {

        if(getSearchQuery().getSearchFilterList().size() == 0) {
            return;
        }
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.
                object("bool").
                array("must");

        for (SearchFilter searchFilter : getSearchQuery().getSearchFilterList()) {

            String elasticField = mapFilterField(searchFilter.getName());
            if(elasticField == null) {
                elasticField = searchFilter.getId();
            }
            if(elasticField == null) {
                throw new IllegalArgumentException("There is no field name defined.");
            }

            for(String value : searchFilter.getValues()) {
                jsonBuilder.stash();
                jsonBuilder.object();
                jsonBuilder.object(searchFilter.getFilterType().getCode());
                jsonBuilder.object(elasticField, value);
                jsonBuilder.unstash();
            }

        }

        ObjectNode query = (ObjectNode) getElasticQuery().get("query").get("filtered");
        if(query == null) {
            throw new JsonBuilderException("There is no filtered query defined in the profile ");
        }
        query.set("filter", jsonBuilder.get());

    }



    public void transformFiltersCurrentVersion() {
        transformFilters("must", FilterOperator.AND);
        transformFiltersOr();
        transformFilters("must_not", FilterOperator.NOT);
    }


    public void transformFilters(String elasticOperator, FilterOperator filterOperator) {
        List<SearchFilter> searchFilterList = getSearchQuery().getSearchFilterList().stream().
                filter(sf -> sf.getFilterOperator().equals(filterOperator)).
                collect(Collectors.toList());
        ArrayNode notFilters = computeFilter(searchFilterList);
        if(notFilters.size() == 0) {
            return;
        }
        // add already defined filters from the profile to the filter array
        ObjectNode filterBool = getFilterBool();
        ArrayNode filter = (ArrayNode) filterBool.get(elasticOperator);

        if(filter != null && filter.isArray()) {
            for (Iterator<JsonNode> it = filter.iterator(); it.hasNext();) {
                notFilters.add(it.next());
            }
        }
        filterBool.set(elasticOperator, notFilters);
    }

    // https://www.elastic.co/guide/en/elasticsearch/reference/current/query-filter-context.html
    // TODO implement range queries for date
    public ArrayNode computeFilter(List<SearchFilter> searchFilterList) {
        ArrayNode filters = getObjectMapper().createArrayNode();
        for (SearchFilter searchFilter : searchFilterList) {
            ArrayNode filter = null;
            switch (searchFilter.getFilterType()) {
                case TERM:
                case MATCH:
                case MATCH_PHRASE:
                    filter = transformTermsFilter(searchFilter);
                    break;
                case RANGE:
                    filter = transformRangeFilter(searchFilter);
                    break;
                case SLIDER:
                    filter = transformRangeFilter(searchFilter);
                    break;
                default:
                    throw new IllegalArgumentException("The filter type " + searchFilter.getFilterType().getCode() + " is not implemented.");
            }


            if(filter != null) {
                filters.addAll(filter);
            }
        }
        return filters;
    }

    public ArrayNode transformTermsFilter(SearchFilter searchFilter) {

        String elasticField = mapFilterField(searchFilter.getName());
        if(elasticField == null) {
            elasticField = searchFilter.getId();
        }
        if(elasticField == null) {
            throw new IllegalArgumentException("There is no field name defined.");
        }

        ArrayNode arrayNode = getObjectMapper().createArrayNode();
        for(String filterValue : searchFilter.getValues()) {
            ObjectNode filter = (ObjectNode) getObjectMapper().createObjectNode().set(searchFilter.getFilterType().getCode(),
                    getObjectMapper().createObjectNode().put(elasticField, filterValue));
            arrayNode.add(filter);
        }

        return arrayNode;

    }

    public ArrayNode transformRangeFilter(SearchFilter searchFilter) {

        String elasticField = getFilterMapping().get(searchFilter.getName());
        if (Strings.isNullOrEmpty(elasticField)) {
            elasticField = searchFilter.getName();
        }
        if(elasticField == null) {
            throw new IllegalArgumentException("Could not create elastic filter because the mapping or the name of " +
                    "the filter is missing");
        }

        ObjectNode range = null;

        if(searchFilter.getFilterDataType().isNumber()) {
            RangeFilterValue<Double> rangeFilterValue = searchFilter.getRangeValue(Double.class);
            range = getObjectMapper().
                    createObjectNode().
                    put(rangeFilterValue.getLowerBound().getOperator(), rangeFilterValue.getMinValue()).
                    put(rangeFilterValue.getUpperBound().getOperator(), rangeFilterValue.getMaxValue());
        } else if (searchFilter.getFilterDataType().isString()) {
            RangeFilterValue<String> rangeFilterValue = searchFilter.getRangeValue(String.class);
            range = getObjectMapper().
                    createObjectNode().
                    put(rangeFilterValue.getLowerBound().getOperator(), rangeFilterValue.getMinValue()).
                    put(rangeFilterValue.getUpperBound().getOperator(), rangeFilterValue.getMaxValue());
        } else if(searchFilter.getFilterDataType().isDate()) {
            RangeFilterValue<Date> rangeFilterValue = searchFilter.getRangeValue(Date.class);
            // TODO transform the date in the correct format
            String minValue = rangeFilterValue.getMinValue().toString();
            String maxValue = rangeFilterValue.getMaxValue().toString();
            range = getObjectMapper().
                    createObjectNode().
                    put(rangeFilterValue.getLowerBound().getOperator(), minValue).
                    put(rangeFilterValue.getUpperBound().getOperator(), maxValue);
        } else {
            throw new IllegalArgumentException("For the data type " + searchFilter.getFilterDataType().getCode() +
                    " no implementation is available.");
        }

        ObjectNode filter = (ObjectNode) getObjectMapper().createObjectNode().set("range",
                getObjectMapper().createObjectNode().set(elasticField, range));

        return getObjectMapper().createArrayNode().add(filter);

    }

    public String mapFilterField(String fieldName) {
        String elasticField = getFilterMapping().get(fieldName);
        if(!Strings.isNullOrEmpty(elasticField)) {
            return elasticField;
        }

        for(Map.Entry<String, String> rule : getFilterRules().entrySet()) {
            String pattern = rule.getKey();
            String replacement = rule.getValue();
            elasticField = fieldName.replaceAll(pattern, replacement);
            if(!Strings.isNullOrEmpty(elasticField)) {
                return elasticField;
            }
        }
        return fieldName;

    }

    public ObjectNode getFilterBool() {
        ObjectNode bool = getBoolQuery();

        ObjectNode filter = (ObjectNode) bool.get("filter");
        if(filter == null) {
            bool = (ObjectNode) bool.set("filter", getObjectMapper().createObjectNode());
            filter = (ObjectNode) bool.get("filter");
        }

        ObjectNode filterBool = (ObjectNode) filter.get("bool");
        if(filterBool == null) {
            filter = (ObjectNode) filter.set("bool", getObjectMapper().createObjectNode());
            filterBool = (ObjectNode) filter.get("bool");
        }
        return filterBool;
    }


    public ObjectNode getBoolQuery() {
        ObjectNode query = (ObjectNode) getElasticQuery().get("query");
        ObjectNode functionScore = (ObjectNode) query.get("function_score");
        if(functionScore != null) {
            query = (ObjectNode) functionScore.get("query");
        }



        ObjectNode bool = (ObjectNode) query.get("bool");
        if(bool == null) {
            bool = getObjectMapper().createObjectNode();
            query.set("bool", bool);
        }
        return bool;
    }



    public void transformFiltersOr() {
        List<SearchFilter> searchFilterList = getSearchQuery().getSearchFilterList().stream().
                filter(sf -> sf.getFilterOperator().equals(FilterOperator.OR)).
                collect(Collectors.toList());

        for(SearchFilter searchFilter : searchFilterList) {
            ArrayNode filters = computeFilter(Arrays.asList(searchFilter));
            if (filters.size() == 0) {
                return;
            }


            ObjectNode should = getObjectMapper().createObjectNode();
            should.set("should", filters);

            ObjectNode bool = getObjectMapper().createObjectNode();

            bool.set("bool", should);

            ObjectNode filterBool = getFilterBool();
            ArrayNode must = (ArrayNode) filterBool.get("must");
            if (must == null) {
                filterBool.set("must", getObjectMapper().createArrayNode());
                must = (ArrayNode) filterBool.get("must");
            }

            must.add(bool);
        }
    }







}
