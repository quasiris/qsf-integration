package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.query.BaseSearchFilter;
import com.quasiris.qsf.query.BoolSearchFilter;
import com.quasiris.qsf.query.Control;
import com.quasiris.qsf.query.Facet;
import com.quasiris.qsf.query.FilterOperator;
import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.Sort;
import com.quasiris.qsf.util.QsfIntegrationConstants;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by mki on 04.02.18.
 */
public class ElasticQsfqlQueryTransformer extends  ElasticParameterQueryTransformer implements QueryTransformerIF {

    private Map<String, String> sortMapping = new HashMap<>();
    private String defaultSort;
    private Map<String, String> filterMapping = new HashMap<>();
    private Map<String, String> filterRules = new HashMap<>();
    private Map<String, String> sortRules = new HashMap<>();
    private Integer defaultRows = 10;
    private Integer rows;
    private Integer defaultPage = 1;

    private Integer elasticVersion = 6;

    private String filterPath;
    private String filterVariable;

    private boolean multiSelectFilter;
    private String variantId;


    @Override
    public ObjectNode transform(PipelineContainer pipelineContainer) throws PipelineContainerException {
        this.pipelineContainer = pipelineContainer;
        if(this.searchQuery == null) {
            this.searchQuery = pipelineContainer.getSearchQuery();
        }

        try {
            transformParameter();
            transformSourceFields();
            transformDebug();
            transformAggregations();
            transformQuery();
            transformSort();
            transformFilters();
            transformPaging();
            replaceParameters();
            collapseResults();
        } catch (JsonBuilderException e) {
            throw new PipelineContainerException(e.getMessage(), e);
        }
        return getElasticQuery();
    }


    @Override
    public void transformAggregations() throws JsonBuilderException {
        if(checkFacetDisabled()) {
            return;
        }

        Map<String, Facet> aggregationsMap = aggregations.stream().
                collect(Collectors.toMap(Facet::getName, Function.identity()));
        if(getSearchQuery().getFacetList() != null) {
            for(Facet facet : getSearchQuery().getFacetList()) {
                Facet aggregation = aggregationsMap.get(facet.getName());
                if(aggregation != null) {
                    // merge
                    aggregation.setFacetFilters(facet.getFacetFilters());

                } else {
                    addAggregation(facet);
                }

            }
        }

        if(Control.isLoadMoreFacets(searchQuery)) {
            for(Facet facet : aggregations) {
                facet.setSize(1000);
            }
        }

        if(multiSelectFilter) {
            transformAggregationsMultiSelect();
        } else {
            super.transformAggregations();
        }


    }

    public void transformAggregationsMultiSelect() throws JsonBuilderException {
        QsfqlFilterMapper filterMapper = new QsfqlFilterMapper();
        filterMapper.setFilterMapping(this.filterMapping);
        filterMapper.setFilterRules(this.filterRules);

        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object();
        boolean hasAggs = false;
        for (Facet aggregation : aggregations) {
            if("slider".equals(aggregation.getType())) {
                ObjectNode filters = getFilterAsJson(filterMapper, aggregation.getId(), null, FilterOperator.OR);
                JsonNode agg = AggregationMapper.createSlider(aggregation, filters);
                jsonBuilder.json(agg);
                hasAggs = true;
            } else {
                ObjectNode filters = getFilterAsJson(filterMapper, aggregation.getId(), aggregation.getFacetFilters(), aggregation.getOperator());
                JsonNode agg = AggregationMapper.createAgg(aggregation, false, filters, variantId);
                jsonBuilder.json(agg);
                hasAggs = true;
            }
        }
        if(StringUtils.isNotEmpty(getVariantId())) {
            // add facet for total doc count
            String fieldName;
            Facet aggregation = new Facet();
            aggregation.setId(getVariantId() +".keyword");
            aggregation.setName(QsfIntegrationConstants.TOTAL_COUNT_AGGREGATION_NAME);
            aggregation.setOperator(FilterOperator.OR);
            aggregation.setFacetFilters(searchQuery.getSearchFilterList());
            aggregation.setType("cardinality");
            ObjectNode filters = getFilterAsJson(filterMapper, aggregation.getId(), aggregation.getFacetFilters(), aggregation.getOperator());
            JsonNode agg = AggregationMapper.createAgg(aggregation, false, filters, null);
            jsonBuilder.json(agg);
            hasAggs = true;
        }

        JsonBuilder aggFilterBuilder = new JsonBuilder().object("bool");
        Map<String, Object> map = new HashMap<>();
        map.put("filter", aggFilterBuilder.get());
        if (hasAggs) {
            map.put("aggs", jsonBuilder.get().deepCopy());
        }
        jsonBuilder = new JsonBuilder();
        jsonBuilder.object("qsc_filtered", map);
        elasticQuery.set("aggs", jsonBuilder.get());
    }

    private ObjectNode getFilterAsJson(QsfqlFilterMapper filterMapper, String id, List<BaseSearchFilter> facetFilter, FilterOperator operator ) throws JsonBuilderException{
        List<BaseSearchFilter> excludeFilters = new ArrayList<>();
        if(operator.equals(FilterOperator.AND)) {
            excludeFilters.addAll(searchQuery.getSearchFilterList());
        } else {
            excludeFilters = deepCopy(searchQuery.getSearchFilterList());
            excludeOwnFilter(excludeFilters, filterMapper, id);
        }
        if(facetFilter != null) {
            excludeFilters.addAll(facetFilter);
        }

        ObjectNode filters = null;
        if(excludeFilters.size() > 0) {
            filters = filterMapper.buildFiltersJson(excludeFilters);
        }
        return filters;
    }

    protected static List<BaseSearchFilter> deepCopy(List<BaseSearchFilter> filters) {
        List<BaseSearchFilter> copy = new ArrayList<>();
        for (BaseSearchFilter filter : filters) {
            BaseSearchFilter clone = SerializationUtils.clone(filter);
            copy.add(clone);
        }
        return copy;
    }

    /**
     * Add all filters except own
     * @param filtersCopy copy of search filters
     * @return filters that will be applied to facet
     */
    private void excludeOwnFilter(List<BaseSearchFilter> filtersCopy, QsfqlFilterMapper filterMapper, String id) {
        Iterator<BaseSearchFilter> it = filtersCopy.iterator();
        while (it.hasNext()) {
            BaseSearchFilter filter = it.next();
            if(filter instanceof SearchFilter) {
                // remove if self
                if(filterMapper.mapFilterField(((SearchFilter)filter).getId()).equals(id)) {
                    it.remove();
                }
            } else if(filter instanceof BoolSearchFilter) {
                excludeOwnFilter(((BoolSearchFilter) filter).getFilters(), filterMapper, id);
            }
        }
    }

    public void transformQuery() {
        // nothing to do - the query is defined in the profile, which is loaded in the ElasticParameterQueryTransformer
    }

    public void transformSort() {
        try {
            Sort sort = getSearchQuery().getSort();
            if(sort == null && defaultSort == null) {
                return;
            }
            if (sort == null) {
                sort = new Sort(defaultSort);
            }

            ArrayNode sortJson = transformSortWithField(sort);
            if(sortJson == null) {
                sortJson = transformSortWithMapping(sort);
            }
            if(sortJson == null) {
                return;
            }
            getElasticQuery().set("sort", sortJson);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected ArrayNode transformSortWithField(Sort sort) throws JsonBuilderException {
        if(sort == null || sort.getField() == null) {
            return null;
        }
        if(sort.getDirection() == null) {
            sort.setDirection("asc");
        }

        String fieldName = sort.getField();
        String elasticField = mapSortField(fieldName);


        String sortJson = "[{\"" + elasticField + "\": \"" + sort.getDirection() + "\"}]";
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.string(sortJson);
        return (ArrayNode) jsonBuilder.get();

    }

    public String mapSortField(String fieldName) {
        for(Map.Entry<String, String> rule : getSortRules().entrySet()) {
            String pattern = rule.getKey();
            String replacement = rule.getValue();
            String elasticField = fieldName.replaceAll(pattern, replacement);
            if(!Strings.isNullOrEmpty(elasticField)) {
                return elasticField;
            }
        }

        String sortTargetField = sortMapping.get(fieldName);
        if(sortTargetField != null) {
            return sortTargetField;
        }
        return fieldName;
    }

    protected ArrayNode transformSortWithMapping(Sort sort) throws JsonBuilderException {
        if(sort == null || sort.getSort() == null) {
            return null;
        }
        String sortJson = sortMapping.get(sort.getSort());
        if(sortJson == null) {
            return null;
        }
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.string(sortJson);
        return (ArrayNode) jsonBuilder.get();
    }

    public void transformFilters() throws JsonBuilderException {
        QsfqlFilterTransformer filterTransformer = new QsfqlFilterTransformer(
                elasticVersion,
                getObjectMapper(),
                getElasticQuery(),
                getSearchQuery(),
                getFilterRules(),
                getFilterMapping(),
                filterPath,
                filterVariable,
                multiSelectFilter
        );
        filterTransformer.transformFilters();
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

    public void collapseResults() {
        if(StringUtils.isNotEmpty(getVariantId())) {
            try {
                String elasticField = getVariantId()+".keyword";
                JsonBuilder jsonBuilder = new JsonBuilder().object("field", elasticField);
                elasticQuery.set("collapse", jsonBuilder.get());
            } catch (JsonBuilderException ignored) {
            }
        }
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

    public void addSortRule(String pattern, String replacement) {
        sortRules.put(pattern, replacement);
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
     * Getter for property 'sortRules'.
     *
     * @return Value for property 'sortRules'.
     */
    public Map<String, String> getSortRules() {
        return sortRules;
    }

    /**
     * Setter for property 'sortRules'.
     *
     * @param sortRules Value to set for property 'sortRules'.
     */
    public void setSortRules(Map<String, String> sortRules) {
        this.sortRules = sortRules;
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

    /**
     * Getter for property 'filterPath'.
     *
     * @return Value for property 'filterPath'.
     */
    public String getFilterPath() {
        return filterPath;
    }

    /**
     * Setter for property 'filterPath'.
     *
     * @param filterPath Value to set for property 'filterPath'.
     */
    public void setFilterPath(String filterPath) {
        this.filterPath = filterPath;
    }

    /**
     * Getter for property 'filterVariable'.
     *
     * @return Value for property 'filterVariable'.
     */
    public String getFilterVariable() {
        return filterVariable;
    }

    /**
     * Setter for property 'filterVariable'.
     *
     * @param filterVariable Value to set for property 'filterVariable'.
     */
    public void setFilterVariable(String filterVariable) {
        this.filterVariable = filterVariable;
    }

    /**
     * Getter for property 'multiSelectFilter'.
     *
     * @return Value for property 'multiSelectFilter'.
     */
    public boolean isMultiSelectFilter() {
        return multiSelectFilter;
    }

    /**
     * Setter for property 'multiSelectFilter'.
     *
     * @param multiSelectFilter Value to set for property 'multiSelectFilter'.
     */
    public void setMultiSelectFilter(boolean multiSelectFilter) {
        this.multiSelectFilter = multiSelectFilter;
    }

    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }
}
