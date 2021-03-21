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
import com.quasiris.qsf.util.DateUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QsfqlFilterMapper {


    private Map<String, String> filterRules = new HashMap<>();

    private Map<String, String> filterMapping = new HashMap<>();

    private ObjectMapper objectMapper = new ObjectMapper();

    public QsfqlFilterMapper() {
    }

    public ArrayNode computeFilterForOperator(FilterOperator filterOperator, List<SearchFilter> searchFilters) throws JsonBuilderException {
        List<SearchFilter> searchFilterList = searchFilters.stream().
                filter(sf -> sf.getFilterOperator().equals(filterOperator)).
                collect(Collectors.toList());
        ArrayNode filters = computeFilter(searchFilterList);
        return filters;

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


    public ArrayNode transformRangeFilter(SearchFilter searchFilter) throws JsonBuilderException {

        String elasticField = mapFilterField(searchFilter.getName());

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
            String minValue = DateUtil.getDate(rangeFilterValue.getMinValue());
            String maxValue = DateUtil.getDate(rangeFilterValue.getMaxValue());
            rangeBuilder.
                    object(rangeFilterValue.getLowerBound().getOperator(), minValue).
                    object(rangeFilterValue.getUpperBound().getOperator(), maxValue);
        } else {
            throw new IllegalArgumentException("For the data type " + searchFilter.getFilterDataType().getCode() +
                    " no implementation is available.");
        }

        return (ArrayNode) rangeBuilder.root().get();

    }

    public ArrayNode createFiltersOr(List<SearchFilter> searchFilters)  throws JsonBuilderException {
        List<SearchFilter> searchFilterList = searchFilters.stream().
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

    public ObjectNode getFilterAsJson(List<SearchFilter> searchFilters) throws JsonBuilderException {
        if(searchFilters.size() == 0) {
            return null;
        }
        ObjectNode filterBool = (ObjectNode) JsonBuilder.create().object().get();
        transformFilters(filterBool, "must", FilterOperator.AND, searchFilters);
        transformFiltersOr(filterBool, searchFilters);
        transformFilters(filterBool, "must_not", FilterOperator.NOT, searchFilters);
        return filterBool;
    }

    public void transformFilters(ObjectNode filterBool, String elasticOperator, FilterOperator filterOperator, List<SearchFilter> searchFilters) throws JsonBuilderException {

        ArrayNode filters = computeFilterForOperator(filterOperator, searchFilters);
        if(filters == null || filters.size() == 0) {
            return;
        }
        ArrayNode filter = (ArrayNode) filterBool.get(elasticOperator);

        if(filter != null && filter.isArray()) {
            for (Iterator<JsonNode> it = filter.iterator(); it.hasNext();) {
                filters.add(it.next());
            }
        }
        filterBool.set(elasticOperator, filters);
    }



    public void transformFiltersOr(ObjectNode filterBool, List<SearchFilter> searchFilters) throws JsonBuilderException {

        ArrayNode orFilters = createFiltersOr(searchFilters);

        if(orFilters == null || orFilters.size() == 0) {
            return;
        }

        ArrayNode must = (ArrayNode) filterBool.get("must");
        if (must == null) {
            filterBool.set("must", objectMapper.createArrayNode());
            must = (ArrayNode) filterBool.get("must");
        }

        must.addAll(orFilters);
    }

    /**
     * Getter for property 'filterRules'.
     *
     * @return Value for property 'filterRules'.
     */
    public Map<String, String> getFilterRules() {
        return filterRules;
    }

    /**
     * Setter for property 'filterRules'.
     *
     * @param filterRules Value to set for property 'filterRules'.
     */
    public void setFilterRules(Map<String, String> filterRules) {
        this.filterRules = filterRules;
    }

    /**
     * Getter for property 'filterMapping'.
     *
     * @return Value for property 'filterMapping'.
     */
    public Map<String, String> getFilterMapping() {
        return filterMapping;
    }

    /**
     * Setter for property 'filterMapping'.
     *
     * @param filterMapping Value to set for property 'filterMapping'.
     */
    public void setFilterMapping(Map<String, String> filterMapping) {
        this.filterMapping = filterMapping;
    }
}
