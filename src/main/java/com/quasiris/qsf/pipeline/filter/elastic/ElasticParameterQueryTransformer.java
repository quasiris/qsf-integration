package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.exception.DebugType;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.pipeline.filter.web.RequestParser;
import com.quasiris.qsf.query.Facet;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.Slider;
import com.quasiris.qsf.util.PrintUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by mki on 04.02.17.
 */
public class ElasticParameterQueryTransformer implements QueryTransformerIF {

    protected PipelineContainer pipelineContainer;

    protected SearchQuery searchQuery;

    protected ObjectNode elasticQuery;

    protected String profile;
    protected Map<String, Object> profileParameter = new HashMap<>();

    protected List<Facet> aggregations = new ArrayList<>();
    protected List<Slider> sliders = new ArrayList<>();

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected Set<String> sourceFields;

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
        } catch (JsonBuilderException e){
            throw new RuntimeException(e);
        }

        return elasticQuery;
    }

    public void transformSourceFields() throws JsonBuilderException {
        if(sourceFields == null) {
            return;
        }

        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.array();

        for(String field : sourceFields) {
            jsonBuilder.addValue(field);
        }
        elasticQuery.set("_source", jsonBuilder.get());
    }

    public void transformDebug() {
        if(pipelineContainer.isDebugEnabled()) {
            getElasticQuery().put("explain", true);
        }
    }

    public void transformAggregations() throws JsonBuilderException {

        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object();
        boolean hasAggs = false;
        for (Facet aggregation : aggregations) {
            JsonNode agg = AggregationMapper.createAgg(aggregation, false);
            jsonBuilder.json(agg);
            hasAggs = true;
        }

        for (Slider slider : sliders) {
            JsonNode agg = AggregationMapper.createSlider(slider);
            jsonBuilder.json(agg);
            hasAggs = true;
        }

        if (hasAggs) {
            elasticQuery.set("aggs", jsonBuilder.get());
        }

    }

    public void transformParameter() {
        Map<String, Object> rawValues = new HashMap<>();
        rawValues.putAll(RequestParser.getRequestParameter(pipelineContainer));


        for(Map.Entry<String, Object> param :profileParameter.entrySet()) {
            rawValues.put("profile." + param.getKey(), param.getValue());
        }

        if(searchQuery != null) {
            if(searchQuery.getQ() != null) {
                rawValues.put("qsfql.q", searchQuery.getQ());
            }

            rawValues.putAll(searchQuery.getParametersWithPrefix("qsfql"));
        }

        for(Map.Entry<String, String> param :pipelineContainer.getParameters().entrySet()) {
            rawValues.put("param." + param.getKey(), param.getValue());
        }

        Map<String, Object> replaceMap = ProfileLoader.encodeParameters(rawValues);


        String request = "";
        try {
            request = loadProfile(replaceMap);
            JsonBuilder jsonBuilder = new JsonBuilder();
            jsonBuilder.string(request);
            for(Map.Entry<String, Object> e : replaceMap.entrySet()) {
                jsonBuilder.valueMap(e.getKey(), e.getValue());
            }
            jsonBuilder.replace();
            elasticQuery = (ObjectNode) jsonBuilder.get();
        }
        catch (Exception e) {
            if(pipelineContainer.isDebugEnabled()) {
                pipelineContainer.debug("profile", DebugType.STRING, request);
                pipelineContainer.debug("replaceMap", DebugType.OBJECT, replaceMap);
            }
            throw new RuntimeException("Could not load elastic query from profile: " + profile, e);
        }

    }

    public String loadProfile(Map<String, Object> values) throws IOException {
        return ProfileLoader.loadProfile(profile, values);
    }

    public StringBuilder print(String indent) {
        StringBuilder printer = new StringBuilder();
        PrintUtil.printKeyValue(printer, indent, "profile", profile);
        return printer;
    }

    public PipelineContainer getPipelineContainer() {
        return pipelineContainer;
    }

    public SearchQuery getSearchQuery() {
        return searchQuery;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setProfileParameter(String key, Object value) {
        this.profileParameter.put(key, value);
    }

    public ObjectNode getElasticQuery() {
        return elasticQuery;
    }

    public void addAggregation(String name, String field) {
        Facet facet = new Facet();
        facet.setName(name);
        facet.setId(field);
        aggregations.add(facet);
    }

    public void addAggregation(String name, String field, int size) {
        Facet facet = new Facet();
        facet.setName(name);
        facet.setId(field);
        facet.setSize(size);
        aggregations.add(facet);
    }

    public void addAggregation(Facet facet) {
        aggregations.add(facet);
    }

    public void addSlider(Slider slider) {
        sliders.add(slider);
    }

    public void addAggregation(String name, String field, int size, String sortOrder, String sortBy, String type) {
        Facet facet = new Facet();
        facet.setName(name);
        facet.setId(field);
        facet.setSortBy(sortBy);
        facet.setSortOrder(sortOrder);
        facet.setSize(size);
        facet.setType(type);
        aggregations.add(facet);
    }

    public String getProfile() {
        return profile;
    }

    /**
     * Setter for property 'sourceFields'.
     *
     * @param sourceFields Value to set for property 'sourceFields'.
     */
    public void setSourceFields(Set<String> sourceFields) {
        this.sourceFields = sourceFields;
    }

    /**
     * Setter for property 'searchQuery'.
     *
     * @param searchQuery Value to set for property 'searchQuery'.
     */
    public void setSearchQuery(SearchQuery searchQuery) {
        this.searchQuery = searchQuery;
    }
}
