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

    private String filterPath;

    private String filterVariable;

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


    public QsfqlFilterTransformer(Integer elasticVersion,
                                  ObjectMapper objectMapper,
                                  ObjectNode elasticQuery,
                                  SearchQuery searchQuery,
                                  Map<String, String> filterRules,
                                  Map<String, String> filterMapping,
                                  String filterPath,
                                  String filterVariable) {
        this.elasticVersion = elasticVersion;
        this.objectMapper = objectMapper;
        this.elasticQuery = elasticQuery;
        this.searchQuery = searchQuery;
        this.filterRules = filterRules;
        this.filterMapping = filterMapping;
        this.filterPath = filterPath;
        this.filterVariable = filterVariable;
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
        if(filterVariable != null) {
            JsonBuilder filterBuilder = JsonBuilder.create();


            ArrayNode filters = computeFilterForOperator(FilterOperator.AND);
            ArrayNode orFilters = createFiltersOr();
            filters.addAll(orFilters);
            if(filters != null && filters.size() > 0) {
                filterBuilder.
                        root().
                        pathsForceCreate("filter/bool").
                        array("must").
                        addJson(filters);
            }

            ArrayNode notFilters = computeFilterForOperator(FilterOperator.NOT);
            if(notFilters != null && notFilters.size() > 0) {
                filterBuilder.
                        root().
                        pathsForceCreate("filter/bool").
                        array("must_not").
                        addJson(notFilters);
            }



            JsonNode node = JsonBuilder.create().
                    newJson(elasticQuery).
                    valueMap(filterVariable, filterBuilder.root().get()).
                    replace().
                    get();
            return;
        }


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


    private ArrayNode computeFilterForOperator(FilterOperator filterOperator) throws JsonBuilderException {
        List<SearchFilter> searchFilterList = getSearchQuery().getSearchFilterList().stream().
                filter(sf -> sf.getFilterOperator().equals(filterOperator)).
                collect(Collectors.toList());
        ArrayNode filters = computeFilter(searchFilterList);
        return filters;

    }

    public void transformFilters(String elasticOperator, FilterOperator filterOperator) throws JsonBuilderException {

        ArrayNode notFilters = computeFilterForOperator(filterOperator);
        if(notFilters == null || notFilters.size() == 0) {
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

    public ObjectNode getFilterBool() throws JsonBuilderException {
        JsonBuilder jsonBuilder = JsonBuilder.create().newJson(getElasticQuery());

        if(filterPath != null) {
            jsonBuilder.pathsForceCreate(filterPath + "/bool");
        } else if(filterPath == null && filterVariable == null && jsonBuilder.exists("query/function_score/query")) {
            throw new IllegalArgumentException("This is not supported anymore. Use filterPath or filterVariable to set the filters.");
            // jsonBuilder.pathsForceCreate("query/function_score/query/bool/filter/bool");
        } else {
            // LOG.warn ...
            jsonBuilder.pathsForceCreate("query/bool/filter/bool");
        }
        ObjectNode bool = (ObjectNode) jsonBuilder.getCurrent();
        return bool;
    }

    private ArrayNode createFiltersOr() throws JsonBuilderException {
        List<SearchFilter> searchFilterList = getSearchQuery().getSearchFilterList().stream().
                filter(sf -> sf.getFilterOperator().equals(FilterOperator.OR)).
                collect(Collectors.toList());

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
        }
        return (ArrayNode) shouldList.root().get();


    }

    public void transformFiltersOr() throws JsonBuilderException {

        ArrayNode orFilters = createFiltersOr();

        if(orFilters == null || orFilters.size() == 0) {
            return;
        }

        ObjectNode filterBool = getFilterBool();
        ArrayNode must = (ArrayNode) filterBool.get("must");
        if (must == null) {
            filterBool.set("must", getObjectMapper().createArrayNode());
            must = (ArrayNode) filterBool.get("must");
        }

        must.addAll(orFilters);
    }

}
