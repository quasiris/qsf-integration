package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.config.DisplayMappingDTO;
import com.quasiris.qsf.config.QsfSearchConfigDTO;
import com.quasiris.qsf.config.QsfSearchConfigUtil;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mki on 04.02.17.
 */
public class ElasticParameterQueryTransformer implements QueryTransformerIF {

    protected PipelineContainer pipelineContainer;

    protected SearchQuery searchQuery;

    protected ObjectNode elasticQuery;

    private QsfSearchConfigDTO searchConfig = QsfSearchConfigUtil.initSearchConfig();

    protected String profile;

    protected Map<String, Object> profileParameter = new HashMap<>();

    private ParameterMapper parameterMapper;

    protected ObjectMapper objectMapper = new ObjectMapper();

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
            replaceParametersDeprecated();
        } catch (JsonBuilderException e){
            throw new RuntimeException(e);
        }

        return elasticQuery;
    }

    // necessary to replace parameters in sorting scripts
    public void replaceParametersDeprecated() throws JsonBuilderException {
        Map<String, Object> valueMap;
        if(parameterMapper == null) {
            valueMap = new HashMap<>();
            if(searchQuery.getParameters() != null) {
                valueMap.putAll(searchQuery.getParameters());
            }
            valueMap.put("query", searchQuery.getQ());
        } else {
            valueMap = parameterMapper.getMappedData();
        }

        elasticQuery = (ObjectNode) JsonBuilder.create().
                valueMap(valueMap).
                newJson(elasticQuery).
                replace().
                get();
    }


    protected Set<String> getSourceFields() throws JsonBuilderException {
        if(searchConfig.getDisplay() == null || searchConfig.getDisplay().getMapping() == null) {
            return new HashSet<>();
        }

        Set<String> sourceFields = searchConfig.getDisplay().getMapping().stream().
                map(DisplayMappingDTO::getFrom).
                collect(Collectors.toSet());

        return sourceFields;
    }

    protected JsonNode buildJsonForSourceFields(Set<String> sourceFields) throws JsonBuilderException {
        if(sourceFields == null || sourceFields.isEmpty()) {
            return null;
        }
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.array();

        for (String sourceField : sourceFields) {
            jsonBuilder.addValue(sourceField);
        }

        return jsonBuilder.get();
    }


    protected Set<String> getVariantSourceFields() throws JsonBuilderException {
        if(searchConfig.getVariant() == null || searchConfig.getVariant().getMapping() == null) {
            return new HashSet<>();
        }

        Set<String> sourceFields = searchConfig.getVariant().getMapping().stream().
                map(DisplayMappingDTO::getFrom).
                collect(Collectors.toSet());

        return sourceFields;
    }

    public void transformSourceFields() throws JsonBuilderException {
        if(!QsfSearchConfigUtil.hasDisplayMapping(searchConfig)) {
            return;
        }
        JsonNode sourceFields = buildJsonForSourceFields(getSourceFields());
        elasticQuery.set("_source", sourceFields);
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
        String variantId = searchConfig.getVariant().getVariantId();
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object();
        boolean hasAggs = false;
        for (Facet aggregation : searchConfig.getFacet().getFacets()) {
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
            ExplainContextHolder.getContext().explainJson("request", request);
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



    public void addAggregation(Facet facet) {
        searchConfig.getFacet().getFacets().add(facet);

    }

    public String getProfile() {
        return profile;
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

    public void setParameterMapper(ParameterMapper parameterMapper) {
        if(parameterMapper != null && getProfileParameter() != null) {
            parameterMapper.getCustomData().putAll(getProfileParameter());
        }
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

    public void setSearchConfig(QsfSearchConfigDTO searchConfig) {
        this.searchConfig = searchConfig;
    }

    public QsfSearchConfigDTO getSearchConfig() {
        return searchConfig;
    }

    public void addSourceField(String sourceField) {
        QsfSearchConfigUtil.initDisplayMapping(searchConfig);
        DisplayMappingDTO mapping = new DisplayMappingDTO();
        mapping.setTo(sourceField);
        mapping.setFrom(sourceField);
        searchConfig.getDisplay().getMapping().add(mapping);
    }
}
