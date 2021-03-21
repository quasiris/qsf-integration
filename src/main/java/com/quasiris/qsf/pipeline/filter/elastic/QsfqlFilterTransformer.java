package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.query.FilterOperator;
import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchQuery;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class QsfqlFilterTransformer {

    private String filterPath;

    private String filterVariable;

    private Integer elasticVersion = 6;

    private ObjectMapper objectMapper;

    private ObjectNode elasticQuery;

    private SearchQuery searchQuery;

    private Map<String, String> filterRules = new HashMap<>();

    private Map<String, String> filterMapping = new HashMap<>();


    private QsfqlFilterMapper filterMapper;

    private boolean multiSelectFilter = false;


    public QsfqlFilterTransformer(ObjectMapper objectMapper, ObjectNode elasticQuery, SearchQuery searchQuery) {
        this.objectMapper = objectMapper;
        this.elasticQuery = elasticQuery;
        this.searchQuery = searchQuery;
        this.filterMapper = new QsfqlFilterMapper(searchQuery.getSearchFilterList());
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
        this.searchQuery = searchQuery;
        this.filterRules = filterRules;
        this.filterMapping = filterMapping;
        this.filterPath = filterPath;
        this.filterVariable = filterVariable;
        this.filterMapper = new QsfqlFilterMapper(searchQuery.getSearchFilterList());
        this.filterMapper.setFilterMapping(filterMapping);
        this.filterMapper.setFilterRules(filterRules);
        this.multiSelectFilter = multiSelectFilter;
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
        if(multiSelectFilter) {
            transformFiltersMultiselect();
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


        ArrayNode filters = filterMapper.computeFilterForOperator(FilterOperator.AND);
        ArrayNode orFilters = filterMapper.createFiltersOr();
        filters.addAll(orFilters);
        if(filters != null && filters.size() > 0) {
            filterBuilder.
                    root().
                    pathsForceCreate("filter/bool").
                    array("must").
                    addJson(filters);
        }

        ArrayNode notFilters = filterMapper.computeFilterForOperator(FilterOperator.NOT);
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

        if(getSearchQuery().getSearchFilterList().size() == 0) {
            return;
        }
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.
                object("bool").
                array("must");

        for (SearchFilter searchFilter : getSearchQuery().getSearchFilterList()) {

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
        if(filterMapper.getSearchFilters().size() == 0) {
            return;
        }
        ObjectNode filterBool = (ObjectNode) JsonBuilder.create().object().get();
        transformFilters(filterBool, "must", FilterOperator.AND);
        transformFiltersOr(filterBool);
        transformFilters(filterBool,"must_not", FilterOperator.NOT);

        elasticQuery = (ObjectNode) JsonBuilder.create().newJson(elasticQuery).pathsForceCreate("query/bool/filter").json("bool", filterBool).get();
    }


    public void transformFiltersMultiselect() throws JsonBuilderException {
        ObjectNode filterBool = (ObjectNode) JsonBuilder.create().object().get();
        transformFilters(filterBool, "must", FilterOperator.AND);
        transformFiltersOr(filterBool);
        transformFilters(filterBool, "must_not", FilterOperator.NOT);

        elasticQuery = (ObjectNode) JsonBuilder.create().newJson(elasticQuery).pathsForceCreate("post_filter").json("bool", filterBool).get();
    }

    public void transformFilters(ObjectNode filterBool, String elasticOperator, FilterOperator filterOperator) throws JsonBuilderException {

        ArrayNode filters = filterMapper.computeFilterForOperator(filterOperator);
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

    public ObjectNode getPostFilter() throws JsonBuilderException {
        JsonBuilder jsonBuilder = JsonBuilder.create().newJson(getElasticQuery());
        jsonBuilder.pathsForceCreate("post_filter/bool");
        ObjectNode postFilter = (ObjectNode) jsonBuilder.getCurrent();
        return postFilter;


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



    public void transformFiltersOr(ObjectNode filterBool) throws JsonBuilderException {

        ArrayNode orFilters = filterMapper.createFiltersOr();

        if(orFilters == null || orFilters.size() == 0) {
            return;
        }

        ArrayNode must = (ArrayNode) filterBool.get("must");
        if (must == null) {
            filterBool.set("must", getObjectMapper().createArrayNode());
            must = (ArrayNode) filterBool.get("must");
        }

        must.addAll(orFilters);
    }

}
