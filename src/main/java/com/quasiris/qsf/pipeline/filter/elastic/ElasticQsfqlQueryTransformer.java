package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.query.*;
import com.quasiris.qsf.util.QsfIntegrationConstants;
import com.quasiris.qsf.util.SearchFilters;
import com.quasiris.qsf.util.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by mki on 04.02.18.
 */
public class ElasticQsfqlQueryTransformer extends  ElasticParameterQueryTransformer implements QueryTransformerIF {

    private Map<String, String> sortMapping = new HashMap<>();
    private String defaultSort;
    private Map<String, String> filterMapping = new HashMap<>();

    private Map<String, Range> definedRangeFilterMapping = new HashMap<>();
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
    protected Set<String> innerHitsSourceFields;

    protected String variantSort;
    protected Integer variantSize;


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
            replaceParametersDeprecated();
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
                collect(Collectors.toMap(Facet::getId, Function.identity()));
        if(getSearchQuery().getFacetList() != null) {
            for(Facet facet : getSearchQuery().getFacetList()) {
                Facet aggregation = aggregationsMap.get(facet.getId());
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
                if(hasLoadMoreFacetsAndSortByNameTag(facet)) {
                    facet.setSize(1000);
                    facet.setSortBy("_key");
                    facet.setSortOrder("asc");
                }
            }
        }

        if(multiSelectFilter) {
            transformAggregationsMultiSelect();
        } else {
            super.transformAggregations();
        }


    }

    boolean hasLoadMoreFacetsAndSortByNameTag(Facet facet) {
        if(facet.getTags() == null) {
            return false;
        }
        return facet.getTags().contains("loadMoreFacetsAndSortByName");
    }

    public void transformAggregationsMultiSelect() throws JsonBuilderException {
        QsfqlFilterMapper filterMapper = new QsfqlFilterMapper();
        filterMapper.setFilterMapping(this.filterMapping);
        filterMapper.setDefinedRangeFilterMapping(this.definedRangeFilterMapping);
        filterMapper.setFilterRules(this.filterRules);

        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object();
        boolean hasAggs = false;
        for (Facet aggregation : aggregations) {
            if("slider".equals(aggregation.getType())) {
                Set<String> excludedFacetIds = new HashSet<>();
                excludedFacetIds.add(aggregation.getId());
                ObjectNode filters = getFilterAsJson(filterMapper, null, FilterOperator.OR, excludedFacetIds);
                JsonNode agg = AggregationMapper.createSlider(aggregation, filters);
                jsonBuilder.json(agg);
                hasAggs = true;

            } else if("range".equals(aggregation.getType())) {
                Set<String> excludedFacetIds = new HashSet<>();
                excludedFacetIds.add(aggregation.getId());
                ObjectNode filters = getFilterAsJson(filterMapper, null, FilterOperator.OR, excludedFacetIds);
                JsonNode agg = AggregationMapper.createRangeFacet((RangeFacet) aggregation, filters);
                jsonBuilder.json(agg);
                hasAggs = true;
            }  else if ("categorySelect".equals(aggregation.getType())) {
                SearchFilter categorySelectFilter = searchQuery.getSearchFilterById(aggregation.getId());
                int level = CategorySelectBuilder.getLevelFromFilter(categorySelectFilter);
                for (int i = 0; i <= level; i++) {
                    createCategorySelectFacet(aggregation, filterMapper, jsonBuilder, i);
                }
                hasAggs = true;
            } else {
                Set<String> excludedFacetIds = new HashSet<>();
                if (aggregation.getExcludeTags() != null) {
                    for (String excludedTag : aggregation.getExcludeTags()) {
                        List<String> excludedIsForTag = aggregations.stream().
                                filter(f -> f.getTags() != null).
                                filter(f -> f.getTags().contains(excludedTag)).
                                map(f -> f.getId()).collect(Collectors.toList());
                        excludedFacetIds.addAll(excludedIsForTag);
                    }
                }
                excludedFacetIds.add(aggregation.getId());

                ObjectNode filters = getFilterAsJson(filterMapper, aggregation.getFacetFilters(), aggregation.getOperator(), excludedFacetIds);
                JsonNode agg = AggregationMapper.createAgg(aggregation, false, filters, variantId, searchQuery);
                jsonBuilder.json(agg);
                hasAggs = true;
            }
        }
        if(StringUtils.isNotEmpty(getVariantId())) {
            // add facet for total doc count
            String fieldName;
            Facet aggregation = new Facet();
            aggregation.setFieldName(getVariantId() +".keyword");
            aggregation.setId(QsfIntegrationConstants.TOTAL_COUNT_AGGREGATION_NAME);
            aggregation.setOperator(FilterOperator.OR);
            aggregation.setType("cardinality");
            Set<String> excludeIds = new HashSet<>();
            excludeIds.add(aggregation.getId());
            ObjectNode filters = getFilterAsJson(filterMapper, aggregation.getFacetFilters(), aggregation.getOperator(), excludeIds);
            JsonNode agg = AggregationMapper.createAgg(aggregation, false, filters, null, searchQuery);
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




    void createCategorySelectFacet(Facet aggregation, QsfqlFilterMapper filterMapper, JsonBuilder jsonBuilder, int level) throws JsonBuilderException {
        Facet categoryTree = new Facet();
        categoryTree.setName(aggregation.getName() + level);

        String filterId = filterMapper.mapFilterField(aggregation.getId() + level);
        categoryTree.setId(filterId);


        List<BaseSearchFilter> searchQueryfilters = SerializationUtils.deepCopyList(searchQuery.getSearchFilterList());
        SearchFilter categorySelectFilter = searchQuery.getSearchFilterById(aggregation.getId());
        if(categorySelectFilter != null) {
            searchQueryfilters = SearchFilters.remove(searchQueryfilters, categorySelectFilter.getId());
            SearchFilter categorySearchFilterForFacet = CategorySelectBuilder.getFilterForLevel(aggregation.getId(), level-1, categorySelectFilter.getValues().get(0));
            if(categorySearchFilterForFacet  != null) {
                searchQueryfilters.add(categorySearchFilterForFacet);
            }
        }

        ObjectNode filters = filterMapper.buildFiltersJson(searchQueryfilters);
        JsonNode agg = AggregationMapper.createAgg(categoryTree, false, filters, variantId, searchQuery);
        jsonBuilder.json(agg);
    }

    private ObjectNode getFilterAsJson(QsfqlFilterMapper filterMapper, List<BaseSearchFilter> facetFilter, FilterOperator operator, Set<String> excludeIds ) throws JsonBuilderException{
        List<BaseSearchFilter> excludeFilters = new ArrayList<>();
        if(operator.equals(FilterOperator.AND)) {
            excludeFilters.addAll(searchQuery.getSearchFilterList());
        } else {
            excludeFilters = SerializationUtils.deepCopyList(searchQuery.getSearchFilterList());
            excludeOwnFilter(excludeFilters, filterMapper, excludeIds);

            // TODO exclude tagged filters
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

    /**
     * Add all filters except own
     * @param filtersCopy copy of search filters
     * @return filters that will be applied to facet
     */
    private void excludeOwnFilter(List<BaseSearchFilter> filtersCopy, QsfqlFilterMapper filterMapper, Set<String> excludeIds) {
        Iterator<BaseSearchFilter> it = filtersCopy.iterator();
        while (it.hasNext()) {
            BaseSearchFilter filter = it.next();
            if(filter instanceof SearchFilter) {
                // remove if self
                // TODO we need a better solution for this workaround

                SearchFilter searchFilter = (SearchFilter) filter;
                if(excludeIds.contains(searchFilter.getId())) {
                    it.remove();
                }
            } else if(filter instanceof BoolSearchFilter) {
                excludeOwnFilter(((BoolSearchFilter) filter).getFilters(), filterMapper, excludeIds);
            }
        }
    }

    /**
     * Add all filters except own
     * @param filtersCopy copy of search filters
     * @return filters that will be applied to facet
     */
    private void excludeOwnFilter(List<BaseSearchFilter> filtersCopy, QsfqlFilterMapper filterMapper, String id) {
        excludeOwnFilter(filtersCopy, filterMapper, Collections.singleton(id));
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
                getDefinedRangeFilterMapping(),
                filterPath,
                filterVariable,
                multiSelectFilter
        );
        filterTransformer.transformFilters();
    }

    public void transformPaging() {
        transformPaging(getElasticQuery(), searchQuery, defaultPage, defaultRows);
    }

    public static void transformPaging(ObjectNode elasticQuery, SearchQuery searchQuery, Integer defaultPage, Integer defaultRows) {
        Integer page = searchQuery.getPage();
        if (page == null) {
            page = defaultPage;
        }
        Integer rows = searchQuery.getRows();
        if (rows == null) {
            rows = defaultRows;
        }
        int start = (page - 1) * rows;

        elasticQuery.put("from", start);
        elasticQuery.put("size", rows);
    }

    public void collapseResults() {
        // https://www.elastic.co/guide/en/elasticsearch/reference/current/collapse-search-results.html
        if(StringUtils.isNotEmpty(getVariantId())) {
            try {
                String elasticField = getVariantId()+".keyword";
                JsonBuilder jsonBuilder = new JsonBuilder().
                        object("field", elasticField);

                int size = 100;
                if(variantSize != null) {
                    size = variantSize;
                }

                if(innerHitsSourceFields != null) {
                    jsonBuilder.object("inner_hits").
                            object("name", "most_recent").
                            object("size", size).
                            object("_source", innerHitsSourceFields);

                    if(variantSort != null) {
                        jsonBuilder.string("sort", variantSort);
                    }
                }

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

    public Set<String> getInnerHitsSourceFields() {
        return innerHitsSourceFields;
    }

    public void setInnerHitsSourceFields(Set<String> innerHitsSourceFields) {
        this.innerHitsSourceFields = innerHitsSourceFields;
    }

    public Map<String, Range> getDefinedRangeFilterMapping() {
        return definedRangeFilterMapping;
    }

    public void setDefinedRangeFilterMapping(Map<String, Range> definedRangeFilterMapping) {
        this.definedRangeFilterMapping = definedRangeFilterMapping;
    }

    public String getVariantSort() {
        return variantSort;
    }

    public void setVariantSort(String variantSort) {
        this.variantSort = variantSort;
    }


    public Integer getVariantSize() {
        return variantSize;
    }

    public void setVariantSize(Integer variantSize) {
        this.variantSize = variantSize;
    }
}
