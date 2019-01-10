package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.query.Facet;
import com.quasiris.qsf.query.RangeFilterValue;
import com.quasiris.qsf.query.SearchFilter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by mki on 04.02.18.
 */
public class ElasticQsfqlQueryTransformer extends  ElasticParameterQueryTransformer implements QueryTransformerIF {

    private Map<String, String> sortMapping = new HashMap<>();
    private String defaultSort;
    private Map<String, String> filterMapping = new HashMap<>();
    private Integer defaultRows = 10;
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
                addAggregation(facet.getName(), facet.getId());
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


    public void transformFiltersVersionOlder2() throws PipelineContainerException {
        ArrayNode filters = getObjectMapper().createArrayNode();
        for (SearchFilter searchFilter : getSearchQuery().getSearchFilterList()) {
            ObjectNode filter = transformTermsFilter(searchFilter);
            if(filter != null) {
                filters.add(filter);
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
    public void transformFiltersCurrentVersion() {
        ArrayNode filters = getObjectMapper().createArrayNode();
        for (SearchFilter searchFilter : getSearchQuery().getSearchFilterList()) {
            ObjectNode filter = null;
            switch (searchFilter.getFilterType()) {
                case TERM:
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
                filters.add(filter);
            }
        }
        if(filters.size() == 0) {
            return;
        }
        ObjectNode query = (ObjectNode) getElasticQuery().get("query").get("bool");

        // add already defined filters from the profile to the filter array
        if(query.get("filter") != null && query.get("filter").isArray()) {
            if(query.get("filter").isArray()) {
                ArrayNode filtersArray = (ArrayNode) query.get("filter");
                for (Iterator<JsonNode> it = filtersArray.iterator(); it.hasNext();) {
                    filters.add((ObjectNode) it.next());
                }
            }
        }

        query.set("filter", filters);

    }

    public ObjectNode transformTermsFilter(SearchFilter searchFilter) {

        String elasticField = getFilterMapping().get(searchFilter.getName());
        if (Strings.isNullOrEmpty(elasticField)) {
            elasticField = searchFilter.getName();
        }

        String firstValue = searchFilter.getValues().stream().findFirst().orElse(null);
        if (Strings.isNullOrEmpty(firstValue)) {
            return null;
        }

        ObjectNode filter = (ObjectNode) getObjectMapper().createObjectNode().set("term",
                getObjectMapper().createObjectNode().put(elasticField, firstValue));

        return filter;

    }

    public ObjectNode transformRangeFilter(SearchFilter searchFilter) {

        String elasticField = getFilterMapping().get(searchFilter.getName());
        if (Strings.isNullOrEmpty(elasticField)) {
            elasticField = searchFilter.getName();
        }

        RangeFilterValue<Double> rangeFilterValue = searchFilter.getRangeValue(Double.class);
        if(rangeFilterValue == null) {
            return null;
        }

        String lowerBoundOperator = rangeFilterValue.getLowerBound().getOperator();
        String upperBoundOperator = rangeFilterValue.getUpperBound().getOperator();

        ObjectNode range = getObjectMapper().createObjectNode().put(lowerBoundOperator, rangeFilterValue.getMinValue()).put(upperBoundOperator, rangeFilterValue.getMaxValue());

        ObjectNode filter = (ObjectNode) getObjectMapper().createObjectNode().set("range",
                getObjectMapper().createObjectNode().set(elasticField, range));

        return filter;

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
}
