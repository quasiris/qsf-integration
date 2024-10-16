package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.exception.DebugType;
import com.quasiris.qsf.explain.ExplainContextHolder;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.mapping.ParameterMapper;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.pipeline.filter.web.RequestParser;
import com.quasiris.qsf.query.Facet;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.util.PrintUtil;

import java.beans.Transient;
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

    private ParameterMapper parameterMapper;

    protected List<Facet> aggregations = new ArrayList<>();

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected Set<String> sourceFields;

    protected String variantId;

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
            replaceParameters();
        } catch (JsonBuilderException e){
            throw new RuntimeException(e);
        }

        return elasticQuery;
    }

    public void replaceParameters() throws JsonBuilderException {
        elasticQuery = (ObjectNode) JsonBuilder.create().
                // TODO
                valueMap("query", searchQuery.getQ()).
                valueMap(searchQuery.getParameters()).

                newJson(elasticQuery).
                replace().
                get();

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
        if(searchQuery.isCtrl("trace")) {
            getElasticQuery().put("explain", true);
        }
    }

    public boolean checkFacetDisabled() {
        return searchQuery.getResult() != null &&
                searchQuery.getResult().getFacet() != null &&
                Boolean.FALSE.equals(searchQuery.getResult().getFacet().getEnabled());
    }

    public void transformAggregations() throws JsonBuilderException {
        if(checkFacetDisabled()) {
            return;
        }

        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object();
        boolean hasAggs = false;
        for (Facet aggregation : aggregations) {
            if("slider".equals(aggregation.getType())) {
                JsonNode agg = AggregationMapper.createSlider(aggregation);
                jsonBuilder.json(agg);
                hasAggs = true;
            } else {
                JsonNode agg = AggregationMapper.createAgg(aggregation, false, null, variantId, searchQuery);
                jsonBuilder.json(agg);
                hasAggs = true;
            }
        }

        if (hasAggs) {
            elasticQuery.set("aggs", jsonBuilder.get());
        }

    }

    private Map<String, Object> loadParameterDeprecated() {
        Map<String, Object> rawValues = new HashMap<>();
        rawValues.putAll(RequestParser.getRequestParameter(pipelineContainer));



        for(Map.Entry<String, Object> param :profileParameter.entrySet()) {
            rawValues.put("profile." + param.getKey(), param.getValue());
        }

        if(searchQuery != null) {
            if(searchQuery.getQ() != null) {
                rawValues.put("qsfql.q", searchQuery.getQ());
            }

            // deprecated remove this in future releases
            rawValues.putAll(searchQuery.getParametersWithPrefix("qsfql"));
            rawValues.putAll(searchQuery.getParametersWithPrefix("qsfql.parameters"));


        }

        for(Map.Entry<String, String> param :pipelineContainer.getParameters().entrySet()) {
            rawValues.put("param." + param.getKey(), param.getValue());
        }

        Map<String, Object> replaceMap = ProfileLoader.encodeParameters(rawValues);

        return replaceMap;
    }

    public void transformParameter() {
        Map<String, Object> replaceMap;
        if(parameterMapper == null) {
            replaceMap = loadParameterDeprecated();
            ExplainContextHolder.getContext().explainJson("loadParameterDeprecated", replaceMap);
        } else {
            replaceMap = parameterMapper.getMappedData();
            ExplainContextHolder.getContext().explainJson("parameterMapper", replaceMap);
        }


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
            }
            throw new RuntimeException("Could not load elastic query from profile: " + profile + " " + e.getMessage(), e);
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

    @Transient
    public PipelineContainer getPipelineContainer() {
        return pipelineContainer;
    }

    @Transient
    public SearchQuery getSearchQuery() {
        return searchQuery;
    }

    @Transient
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void addProfileParameter(String key, Object value) {
        this.profileParameter.put(key, value);
    }

    public Map<String, Object> getProfileParameter() {
        return profileParameter;
    }

    public void setProfileParameter(Map<String, Object> profileParameter) {
        this.profileParameter = profileParameter;
    }

    @Transient
    public ObjectNode getElasticQuery() {
        return elasticQuery;
    }

    public void addAggregation(String name, String id, String field) {
        Facet facet = new Facet();
        facet.setName(name);
        facet.setId(id);
        facet.setFieldName(field);
        aggregations.add(facet);
    }

    public void addAggregation(String name, String id, String field, int size) {
        Facet facet = new Facet();
        facet.setName(name);
        facet.setId(id);
        facet.setFieldName(field);
        facet.setSize(size);
        aggregations.add(facet);
    }

    public void addAggregation(Facet facet) {
        aggregations.add(facet);

    }

    public void addAggregation(String name, String id, String field, int size, String sortOrder, String sortBy, String type) {
        Facet facet = new Facet();
        facet.setName(name);
        facet.setId(id);
        facet.setFieldName(field);
        facet.setSortBy(sortBy);
        facet.setSortOrder(sortOrder);
        facet.setSize(size);
        facet.setType(type);
        aggregations.add(facet);
    }

    public List<Facet> getAggregations() {
        return aggregations;
    }

    public void setAggregations(List<Facet> aggregations) {
        this.aggregations = aggregations;
    }

    public String getProfile() {
        return profile;
    }

    public Set<String> getSourceFields() {
        return sourceFields;
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
    @Transient
    public void setSearchQuery(SearchQuery searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }

    public void setParameterMapper(ParameterMapper parameterMapper) {
        this.parameterMapper = parameterMapper;
    }

    public void addCustomParameter(String key, Object value) {
        if(parameterMapper == null) {
            return;
        }
        parameterMapper.addCustomData(key, value);
    }

    public void addCustomParameter(Map<String, Object> parameters) {
        if(parameterMapper == null) {
            return;
        }
        parameterMapper.addCustomData(parameters);
    }
}
