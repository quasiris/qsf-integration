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



    public void transformFiltersCurrentVersion() throws JsonBuilderException {
        transformFilters("must", FilterOperator.AND);
        transformFiltersOr();
        transformFilters("must_not", FilterOperator.NOT);
    }


    public void transformFilters(String elasticOperator, FilterOperator filterOperator) throws JsonBuilderException {
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
    public ArrayNode computeFilter(List<SearchFilter> searchFilterList) throws JsonBuilderException {
        JsonBuilder filters = JsonBuilder.create().array();
        for (SearchFilter searchFilter : searchFilterList) {
            filters.stash();
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
                filters.addJson(filter);
            }
            filters.unstash();
        }
        return (ArrayNode) filters.get();
    }

    public ArrayNode transformTermsFilter(SearchFilter searchFilter) throws JsonBuilderException {

        String elasticField = mapFilterField(searchFilter.getName());
        if(elasticField == null) {
            elasticField = searchFilter.getId();
        }
        if(elasticField == null) {
            throw new IllegalArgumentException("There is no field name defined.");
        }

        JsonBuilder jsonBuilder = JsonBuilder.create().array();

        for(String filterValue : searchFilter.getValues()) {
            jsonBuilder.stash();
            jsonBuilder.object(searchFilter.getFilterType().getCode());
            jsonBuilder.object(elasticField, filterValue);
            jsonBuilder.unstash();
        }
        return (ArrayNode) jsonBuilder.unstash().get();

    }

    public ArrayNode transformRangeFilter(SearchFilter searchFilter) throws JsonBuilderException {

        String elasticField = getFilterMapping().get(searchFilter.getName());
        if (Strings.isNullOrEmpty(elasticField)) {
            elasticField = searchFilter.getName();
        }
        if(elasticField == null) {
            throw new IllegalArgumentException("Could not create elastic filter because the mapping or the name of " +
                    "the filter is missing");
        }

        JsonBuilder rangeBuilder = JsonBuilder.create().
            array().
            object("range").
            object(elasticField);

        if(searchFilter.getFilterDataType().isNumber()) {
            RangeFilterValue<Double> rangeFilterValue = searchFilter.getRangeValue(Double.class);
            rangeBuilder.
                    object(rangeFilterValue.getLowerBound().getOperator(), rangeFilterValue.getMinValue()).
                    object(rangeFilterValue.getUpperBound().getOperator(), rangeFilterValue.getMaxValue());
        } else if (searchFilter.getFilterDataType().isString()) {
            RangeFilterValue<String> rangeFilterValue = searchFilter.getRangeValue(String.class);
            rangeBuilder.
                    object(rangeFilterValue.getLowerBound().getOperator(), rangeFilterValue.getMinValue()).
                    object(rangeFilterValue.getUpperBound().getOperator(), rangeFilterValue.getMaxValue());

        } else if(searchFilter.getFilterDataType().isDate()) {
            RangeFilterValue<Date> rangeFilterValue = searchFilter.getRangeValue(Date.class);
            // TODO transform the date in the correct format
            String minValue = rangeFilterValue.getMinValue().toString();
            String maxValue = rangeFilterValue.getMaxValue().toString();
            rangeBuilder.
                    object(rangeFilterValue.getLowerBound().getOperator(), minValue).
                    object(rangeFilterValue.getUpperBound().getOperator(), maxValue);
        } else {
            throw new IllegalArgumentException("For the data type " + searchFilter.getFilterDataType().getCode() +
                    " no implementation is available.");
        }

        return (ArrayNode) rangeBuilder.root().get();

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



    public void transformFiltersOr() throws JsonBuilderException {
        List<SearchFilter> searchFilterList = getSearchQuery().getSearchFilterList().stream().
                filter(sf -> sf.getFilterOperator().equals(FilterOperator.OR)).
                collect(Collectors.toList());

        boolean hasFilter = false;

        JsonBuilder shouldList = JsonBuilder.create().array();
        for(SearchFilter searchFilter : searchFilterList) {
            JsonBuilder shouldBuilder = JsonBuilder.create().
                    object("bool").
                    array("should");
            ArrayNode filters = computeFilter(Arrays.asList(searchFilter));
            if (filters.size() == 0) {
                continue;
            }
            shouldBuilder.addJson(filters);
            shouldList.addJson(shouldBuilder.root().get());
            hasFilter = true;
        }

        if(!hasFilter) {
            return;
        }


        ObjectNode filterBool = getFilterBool();
        ArrayNode must = (ArrayNode) filterBool.get("must");
        if (must == null) {
            filterBool.set("must", getObjectMapper().createArrayNode());
            must = (ArrayNode) filterBool.get("must");
        }

        must.addAll((ArrayNode) shouldList.root().get());
    }

}
