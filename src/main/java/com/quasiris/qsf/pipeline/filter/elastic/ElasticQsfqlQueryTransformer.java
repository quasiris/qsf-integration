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

        Map<String, Facet> aggregationsMap = getSearchConfig().getFacet().getFacets().stream()
                .collect(Collectors.toMap(
                        Facet::getId,
                        Function.identity(),
                        (existing, duplicate) -> existing // Keep the first occurrence and ignore duplicates
                ));
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
            for(Facet facet : getSearchConfig().getFacet().getFacets()) {
                if(hasLoadMoreFacetsAndSortByNameTag(facet)) {
                    facet.setSize(1000);
                    facet.setSortBy("_key");
                    facet.setSortOrder("asc");
                }
            }
        }

        if(getSearchConfig().getFilter().getMultiSelectFilter()) {
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
        QsfqlFilterMapper filterMapper = new QsfqlFilterMapper(getSearchConfig());

        String variantId = getSearchConfig().getVariant().getVariantId();
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object();
        boolean hasAggs = false;
        for (Facet aggregation : getSearchConfig().getFacet().getFacets()) {
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
                        List<String> excludedIsForTag = getSearchConfig().getFacet().getFacets().stream().
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
        if(StringUtils.isNotEmpty(variantId)) {
            // add facet for total doc count
            String fieldName;
            Facet aggregation = new Facet();
            aggregation.setFieldName(variantId +".keyword");
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

        String variantId = getSearchConfig().getVariant().getVariantId();

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
            String defaultSort = getSearchConfig().getSort().getDefaultSort();
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
        for(Map.Entry<String, String> rule : getSearchConfig().getSort().getSortRules().entrySet()) {
            String pattern = rule.getKey();
            String replacement = rule.getValue();
            String elasticField = fieldName.replaceAll(pattern, replacement);
            if(!Strings.isNullOrEmpty(elasticField)) {
                return elasticField;
            }
        }

        String sortTargetField = getSearchConfig().getSort().getSortMapping().get(fieldName);
        if(sortTargetField != null) {
            return sortTargetField;
        }
        return fieldName;
    }

    protected ArrayNode transformSortWithMapping(Sort sort) throws JsonBuilderException {
        if(sort == null || sort.getSort() == null) {
            return null;
        }
        String sortJson = getSearchConfig().getSort().getSortMapping().get(sort.getSort());
        if(sortJson == null) {
            return null;
        }
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.string(sortJson);
        return (ArrayNode) jsonBuilder.get();
    }

    public void transformFilters() throws JsonBuilderException {
        QsfqlFilterTransformer filterTransformer = new QsfqlFilterTransformer(
                getObjectMapper(),
                getElasticQuery(),
                getSearchQuery(),
                getSearchConfig()
        );
        filterTransformer.transformFilters();
    }

    public void transformPaging() {
        Integer defaultPage = getSearchConfig().getPaging().getDefaultPage();
        Integer defaultRows = getSearchConfig().getPaging().getDefaultRows();
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
        String variantId = getSearchConfig().getVariant().getVariantId();
        if(StringUtils.isNotEmpty(variantId)) {
            try {
                String elasticField = variantId+".keyword";
                JsonBuilder jsonBuilder = new JsonBuilder().
                        object("field", elasticField);

                Integer variantSize = getSearchConfig().getVariant().getVariantSize();
                JsonNode innerHitsSourceFields = getVariantSourceFields();
                String variantSort = getSearchConfig().getVariant().getVariantSort();
                boolean queryInnerhits = false;

                if(variantSort != null) {
                    queryInnerhits = true;
                }

                if(innerHitsSourceFields != null ) {
                    queryInnerhits = true;
                }

                if(variantSize != null && variantSize > 1 ) {
                    queryInnerhits = true;
                }

                if(queryInnerhits) {
                    int size = 100;
                    if(variantSize != null) {
                        size = variantSize;
                    }

                    if(innerHitsSourceFields == null) {
                        innerHitsSourceFields = getSourceFields();
                    }

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
        getSearchConfig().getSort().getSortMapping().put(from, to);
    }

    public void addFilterMapping(String from, String to) {
        getSearchConfig().getFilter().getFilterMapping().put(from, to);
    }


    public void addFilterRule(String pattern, String replacement) {
        getSearchConfig().getFilter().getFilterRules().put(pattern, replacement);
    }

    public void addSortRule(String pattern, String replacement) {
        getSearchConfig().getSort().getSortRules().put(pattern, replacement);
    }
}
