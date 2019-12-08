package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.query.Facet;
import com.quasiris.qsf.query.FilterOperator;
import com.quasiris.qsf.query.RangeFilterValue;
import com.quasiris.qsf.query.SearchFilter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mki on 04.02.18.
 */
public class ElasticQsfqlQueryTransformer extends  ElasticParameterQueryTransformer implements QueryTransformerIF {

    private Map<String, String> sortMapping = new HashMap<>();
    private String defaultSort;
    private Map<String, String> filterMapping = new HashMap<>();
    private Map<String, String> filterRules = new HashMap<>();
    private Integer defaultRows = 10;
    private Integer rows;
    private Integer defaultPage = 1;

    private Integer elasticVersion = 6;


    @Override
    public ObjectNode transform(PipelineContainer pipelineContainer) throws PipelineContainerException {
        super.transform(pipelineContainer);

        transformQuery();
        transformSort();
        transformFilters();
        transformPaging();


        return getElasticQuery();
    }


    @Override
    public void transformAggregations() {
        if(getSearchQuery().getFacetList() != null) {
            for(Facet facet : getSearchQuery().getFacetList()) {
                addAggregation(facet);
            }
        }
        super.transformAggregations();

    }

    public void transformQuery() {
        // nothing to do - the query is defined in the profile, which is loaded in the ElasticParameterQueryTransformer
    }

    public void transformSort() {
        try {
            String sort = null;
            if (getSearchQuery().getSort() == null) {
                sort = sortMapping.get(defaultSort);
            } else {
                sort = sortMapping.get(getSearchQuery().getSort().getSort());
                if (sort == null) {
                    sort = sortMapping.get(defaultSort);
                }
            }
            if (sort == null) {
                return;
            }
            ArrayNode sortJson = (ArrayNode) getObjectMapper().readTree(sort);
            getElasticQuery().set("sort", sortJson);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void transformFilters() throws PipelineContainerException {
        if(elasticVersion < 2) {
            transformFiltersVersionOlder2();
            return;
        }
        transformFiltersCurrentVersion();
    }

    // https://www.elastic.co/guide/en/elasticsearch/reference/current/query-filter-context.html
    // TODO implement range queries for date
    public void transformFiltersCurrentVersion() {
        transformFilters("must", FilterOperator.AND);
        transformFiltersOr();
        transformFilters("must_not", FilterOperator.NOT);
    }

    public void transformFiltersVersionOlder2() throws PipelineContainerException {
        ArrayNode filters = getObjectMapper().createArrayNode();
        for (SearchFilter searchFilter : getSearchQuery().getSearchFilterList()) {
            ArrayNode filter = transformTermsFilter(searchFilter);
            if(filter != null) {
                filters.addAll(filter);
            }
        }
        if(filters.size() == 0) {
            return;
        }

        ObjectNode must = getObjectMapper().createObjectNode();
        must.set("must", filters);

        ObjectNode bool = getObjectMapper().createObjectNode();
        bool.set("bool", must);
        ObjectNode query = (ObjectNode) getElasticQuery().get("query").get("filtered");
        if(query == null) {
            throw new PipelineContainerException("There is no filtered query defined in the profile " + getProfile());
        }
        query.set("filter", bool);

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

    public ObjectNode getBoolQuery() {
        ObjectNode query = (ObjectNode) getElasticQuery().get("query");
        ObjectNode functionScore = (ObjectNode) query.get("function_score");
        if(functionScore != null) {
            query = (ObjectNode) functionScore.get("query");
        }



        ObjectNode bool = (ObjectNode) query.get("bool");
        if(bool == null) {
            bool = objectMapper.createObjectNode();
            query.set("bool", bool);
        }
        return bool;
    }


    public ObjectNode getFilterBool() {
        ObjectNode bool = getBoolQuery();

        ObjectNode filter = (ObjectNode) bool.get("filter");
        if(filter == null) {
            bool = (ObjectNode) bool.set("filter", objectMapper.createObjectNode());
            filter = (ObjectNode) bool.get("filter");
        }

        ObjectNode filterBool = (ObjectNode) filter.get("bool");
        if(filterBool == null) {
            filter = (ObjectNode) filter.set("bool", objectMapper.createObjectNode());
            filterBool = (ObjectNode) filter.get("bool");
        }
        return filterBool;
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


    public void transformFiltersOr() {
        List<SearchFilter> searchFilterList = getSearchQuery().getSearchFilterList().stream().
                filter(sf -> sf.getFilterOperator().equals(FilterOperator.OR)).
                collect(Collectors.toList());

        for(SearchFilter searchFilter : searchFilterList) {
            ArrayNode filters = computeFilter(Arrays.asList(searchFilter));
            if (filters.size() == 0) {
                return;
            }


            ObjectNode should = objectMapper.createObjectNode();
            should.set("should", filters);

            ObjectNode bool = objectMapper.createObjectNode();

            bool.set("bool", should);

            ObjectNode filterBool = getFilterBool();
            ArrayNode must = (ArrayNode) filterBool.get("must");
            if (must == null) {
                filterBool.set("must", objectMapper.createArrayNode());
                must = (ArrayNode) filterBool.get("must");
            }

            must.add(bool);
        }



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

    public void transformPaging() {
        Integer page = getSearchQuery().getPage();
        if (page == null) {
            page = defaultPage;
        }
        Integer rows = getSearchQuery().getRows();
        if (rows == null) {
            rows = defaultRows;
        }
        if(this.rows != null) {
            rows = this.rows;
        }


        int start = (page - 1) * rows;

        getElasticQuery().put("from", start);
        getElasticQuery().put("size", rows);
    }



    @Override
    public StringBuilder print(String indent) {
        return new StringBuilder("TODO");
    }

    public void addSortMapping(String from, String to) {
        sortMapping.put(from, to);
    }

    public void addFilterMapping(String from, String to) {
        filterMapping.put(from, to);
    }


    public void addFilterRule(String pattern, String replacement) {
        filterRules.put(pattern, replacement);
    }

    public Map<String, String> getSortMapping() {
        return sortMapping;
    }

    public void setSortMapping(Map<String, String> sortMapping) {
        this.sortMapping = sortMapping;
    }

    public String getDefaultSort() {
        return defaultSort;
    }

    public void setDefaultSort(String defaultSort) {
        this.defaultSort = defaultSort;
    }

    public Map<String, String> getFilterMapping() {
        return filterMapping;
    }

    public void setFilterMapping(Map<String, String> filterMapping) {
        this.filterMapping = filterMapping;
    }

    public Integer getDefaultRows() {
        return defaultRows;
    }

    public void setDefaultRows(Integer defaultRows) {
        this.defaultRows = defaultRows;
    }

    public Integer getDefaultPage() {
        return defaultPage;
    }

    public void setDefaultPage(Integer defaultPage) {
        this.defaultPage = defaultPage;
    }

    public Integer getElasticVersion() {
        return elasticVersion;
    }

    public void setElasticVersion(Integer elasticVersion) {
        this.elasticVersion = elasticVersion;
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
     * Getter for property 'rows'.
     *
     * @return Value for property 'rows'.
     */
    public Integer getRows() {
        return rows;
    }

    /**
     * Setter for property 'rows'.
     *
     * @param rows Value to set for property 'rows'.
     */
    public void setRows(Integer rows) {
        this.rows = rows;
    }
}
