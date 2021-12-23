package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.query.BaseSearchFilter;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.SearchQueryFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QsfqlFilterTransformer {

    private String filterPath;

    private String filterVariable;

    private Integer elasticVersion = 6;

    private ObjectMapper objectMapper;

    private ObjectNode elasticQuery;

    private SearchQuery searchQuery;

    private Map<String, String> filterRules;

    private Map<String, String> filterMapping;


    private QsfqlFilterMapper filterMapper;

    private boolean multiSelectFilter;


    public QsfqlFilterTransformer(ObjectMapper objectMapper, ObjectNode elasticQuery, SearchQuery searchQuery) {
        this.objectMapper = objectMapper;
        this.elasticQuery = elasticQuery;
        this.searchQuery = SearchQueryFactory.deepCopy(searchQuery);
        this.filterMapper = new QsfqlFilterMapper();
    }


    public QsfqlFilterTransformer(Integer elasticVersion,
                                  ObjectMapper objectMapper,
                                  ObjectNode elasticQuery,
                                  SearchQuery searchQuery,
                                  Map<String, String> filterRules,
                                  Map<String, String> filterMapping,
                                  String filterPath,
                                  String filterVariable,
                                  boolean multiSelectFilter
    ) {
        this.elasticVersion = elasticVersion;
        this.objectMapper = objectMapper;
        this.elasticQuery = elasticQuery;
        this.searchQuery = SearchQueryFactory.deepCopy(searchQuery);
        this.filterRules = filterRules;
        this.filterMapping = filterMapping;
        this.filterPath = filterPath;
        this.filterVariable = filterVariable;

        this.filterMapper = new QsfqlFilterMapper();
        this.filterMapper.setFilterMapping(filterMapping);
        this.filterMapper.setFilterRules(filterRules);

        this.multiSelectFilter = multiSelectFilter;
    }


    public Map<String, String> getFilterRules() {
        return filterRules;
    }


    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public Map<String, String> getFilterMapping() {
        return filterMapping;
    }




    public void transformFilters() throws JsonBuilderException {
        JsonBuilder resultJsonBuilder = JsonBuilder.create().newJson(elasticQuery);

        // post filters
        List<BaseSearchFilter> postFilters = new ArrayList<>();
        if(multiSelectFilter) {
            transformFiltersMultiselect(postFilters);
        }
        if(postFilters.size() > 0) {
            ObjectNode postFilterNodes = compileFilters(postFilters);
            resultJsonBuilder = JsonBuilder.create().newJson(resultJsonBuilder.replace().get()).pathsForceCreate("post_filter").json(postFilterNodes);
        }

        // filters
        if(searchQuery.getSearchFilterList() != null) {
            // compute filters
            ObjectNode filters = compileFilters(searchQuery.getSearchFilterList());

            // apply filters to query
            if (StringUtils.isNotBlank(filterVariable)) {
                // put filters into filtersVariable placeholder
                ObjectNode filterObj = filters != null ? filters : (ObjectNode) JsonBuilder.create().object().get();
                resultJsonBuilder.valueMap(filterVariable, JsonBuilder.create().object("filter", filterObj).get());
            } else if(searchQuery.getSearchFilterList().size() > 0) {
                // append filters to defined path
                JsonNode filterNode = JsonBuilder.create().newJson(resultJsonBuilder.replace().get()).pathsForceCreate("query/bool/filter").json(filters).get();
                resultJsonBuilder.json(filterNode);
            }
        }

        elasticQuery = (ObjectNode) resultJsonBuilder.replace().get();
    }

    public ObjectNode compileFilters(List<BaseSearchFilter> searchFilters) throws JsonBuilderException {
        return filterMapper.buildFiltersJson(searchFilters);
    }

    protected void transformFiltersMultiselect(List<BaseSearchFilter> postFilters) {
        if(searchQuery.getSearchFilterList().size() > 0) {
            postFilters.addAll(searchQuery.getSearchFilterList());
            searchQuery.setSearchFilterList(new ArrayList<>()); // clear filters
        }
    }

    public ObjectNode getElasticQuery() {
        return elasticQuery;
    }
}
