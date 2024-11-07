package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.config.QsfSearchConfigDTO;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.query.BaseSearchFilter;
import com.quasiris.qsf.query.Range;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.SearchQueryFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QsfqlFilterTransformer {

    private ObjectMapper objectMapper;

    private ObjectNode elasticQuery;

    private SearchQuery searchQuery;

    private QsfSearchConfigDTO searchConfig;

    private QsfqlFilterMapper filterMapper;

    public QsfqlFilterTransformer(ObjectMapper objectMapper, ObjectNode elasticQuery, SearchQuery searchQuery, QsfSearchConfigDTO searchConfig) {
        this.objectMapper = objectMapper;
        this.elasticQuery = elasticQuery;
        this.searchQuery = SearchQueryFactory.deepCopy(searchQuery);
        this.filterMapper = new QsfqlFilterMapper(searchConfig);
        this.searchConfig = searchConfig;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }


    public void transformFilters() throws JsonBuilderException {
        JsonBuilder resultJsonBuilder = JsonBuilder.create().newJson(elasticQuery);

        // post filters
        List<BaseSearchFilter> postFilters = new ArrayList<>();
        if(searchConfig.getFilter().getMultiSelectFilter()) {
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
            if (StringUtils.isNotBlank(searchConfig.getFilter().getFilterVariable())) {
                // put filters into filtersVariable placeholder
                ObjectNode filterObj = filters != null ? filters : (ObjectNode) JsonBuilder.create().object().get();
                resultJsonBuilder.valueMap(searchConfig.getFilter().getFilterVariable(), JsonBuilder.create().object("filter", filterObj).get());
            } else if(searchQuery.getSearchFilterList().size() > 0) {
                // append filters to defined path
                if(resultJsonBuilder.exists("query/bool/filter")) {
                    resultJsonBuilder.path("query/bool/filter").addJson(filters);
                } else {
                    JsonNode filterNode = JsonBuilder.create().newJson(resultJsonBuilder.replace().get()).pathsForceCreate("query/bool/filter").json(filters).get();
                    resultJsonBuilder.json(filterNode);
                }
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
