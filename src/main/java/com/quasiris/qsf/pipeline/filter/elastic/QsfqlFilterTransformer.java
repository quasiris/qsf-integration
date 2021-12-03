package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.query.FilterOperator;
import com.quasiris.qsf.query.FilterType;
import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchQuery;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
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
    private String variantId;


    public QsfqlFilterTransformer(ObjectMapper objectMapper, ObjectNode elasticQuery, SearchQuery searchQuery) {
        this.objectMapper = objectMapper;
        this.elasticQuery = elasticQuery;
        this.searchQuery = searchQuery;
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
                                  boolean multiSelectFilter,
                                  String variantId
    ) {
        this.elasticVersion = elasticVersion;
        this.objectMapper = objectMapper;
        this.elasticQuery = elasticQuery;
        this.searchQuery = searchQuery;
        this.filterRules = filterRules;
        this.filterMapping = filterMapping;
        this.filterPath = filterPath;
        this.filterVariable = filterVariable;

        this.filterMapper = new QsfqlFilterMapper();
        this.filterMapper.setFilterMapping(filterMapping);
        this.filterMapper.setFilterRules(filterRules);

        this.multiSelectFilter = multiSelectFilter;
        this.variantId = variantId;
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
        // filter for variants here
        if(multiSelectFilter) {
            List<SearchFilter> postFilters = new ArrayList<>();
            transformFiltersMultiselect(postFilters);
            if(StringUtils.isNotEmpty(variantId)) {
                filterVariants(postFilters);
            }
            appendPostFilter(postFilters);
            return;
        }

        if(filterVariable != null) {
            transformFiltersWithVariable();
            return;
        }


        if(elasticVersion < 2) {
            transformFiltersVersionOlder2();
            return;
        }
        transformFiltersCurrentVersion();
    }

    public void transformFiltersWithVariable() throws JsonBuilderException {
        JsonBuilder filterBuilder = JsonBuilder.create();


        ArrayNode filters = filterMapper.computeFilterForOperator(FilterOperator.AND, searchQuery.getSearchFilterList());
        ArrayNode orFilters = filterMapper.createFiltersOr(searchQuery.getSearchFilterList());
        filters.addAll(orFilters);
        if(filters != null && filters.size() > 0) {
            filterBuilder.
                    root().
                    pathsForceCreate("filter/bool").
                    array("must").
                    addJson(filters);
        }

        ArrayNode notFilters = filterMapper.computeFilterForOperator(FilterOperator.NOT, searchQuery.getSearchFilterList());
        if(notFilters != null && notFilters.size() > 0) {
            filterBuilder.
                    root().
                    pathsForceCreate("filter/bool").
                    array("must_not").
                    addJson(notFilters);
        }

        JsonBuilder.create().
                newJson(elasticQuery).
                valueMap(filterVariable, filterBuilder.root().get()).
                replace().
                get();
    }

    public void transformFiltersVersionOlder2() throws JsonBuilderException {

        if(searchQuery.getSearchFilterList().size() == 0) {
            return;
        }
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.
                object("bool").
                array("must");

        for (SearchFilter searchFilter : searchQuery.getSearchFilterList()) {

            String elasticField = filterMapper.mapFilterField(searchFilter.getName());
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
        if(searchQuery.getSearchFilterList().size() > 0) {
            ObjectNode filters = filterMapper.getFilterAsJson(searchQuery.getSearchFilterList());
            elasticQuery = (ObjectNode) JsonBuilder.create().newJson(elasticQuery).pathsForceCreate("query/bool/filter").json("bool", filters).get();
        }
    }



    public void transformFiltersMultiselect(List<SearchFilter> postFilters) {
        if(searchQuery.getSearchFilterList().size() > 0) {
            postFilters.addAll(searchQuery.getSearchFilterList());
        }
    }

    public void filterVariants(List<SearchFilter> postFilters) {
        SearchFilter variantFilter = new SearchFilter();
        variantFilter.setName("docType");
        variantFilter.setValues(Arrays.asList("summary"));
        variantFilter.setFilterType(FilterType.TERM);
        variantFilter.setFilterOperator(FilterOperator.NOT);

        postFilters.add(variantFilter);
    }

    public void appendPostFilter(List<SearchFilter> postFilters) throws JsonBuilderException {
        if(postFilters.size() > 0) {
            ObjectNode postFilterNodes = filterMapper.getFilterAsJson(postFilters);
            elasticQuery = (ObjectNode) JsonBuilder.create().newJson(elasticQuery).pathsForceCreate("post_filter").object("bool", postFilterNodes).get();
        }
    }

    public ObjectNode getElasticQuery() {
        return elasticQuery;
    }
}
