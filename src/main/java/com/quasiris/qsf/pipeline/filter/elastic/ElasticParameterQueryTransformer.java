package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.quasiris.qsf.exception.DebugType;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.pipeline.filter.web.RequestParser;
import com.quasiris.qsf.query.Facet;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.Slider;
import com.quasiris.qsf.util.ElasticUtil;
import com.quasiris.qsf.util.JsonUtil;
import com.quasiris.qsf.util.PrintUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by mki on 04.02.17.
 */
public class ElasticParameterQueryTransformer implements QueryTransformerIF {

    protected PipelineContainer pipelineContainer;

    protected SearchQuery searchQuery;

    protected ObjectNode elasticQuery;

    protected String profile;
    protected Map<String, String> profileParameter = new HashMap<>();

    protected List<Facet> aggregations = new ArrayList<>();
    protected List<Slider> sliders = new ArrayList<>();

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected Set<String> sourceFields;



    @Override
    public ObjectNode transform(PipelineContainer pipelineContainer) throws PipelineContainerException {
        this.pipelineContainer = pipelineContainer;
        this.searchQuery = pipelineContainer.getSearchQuery();


        transformParameter();
        transformSourceFields();
        transformAggregations();

        return elasticQuery;
    }

    public void transformSourceFields() {
        if(sourceFields == null) {
            return;
        }
        ArrayNode sourceFieldArray = objectMapper.createArrayNode();
        for(String field : sourceFields) {
            sourceFieldArray.add(field);
        }
        elasticQuery.set("_source", sourceFieldArray);
    }

    public void transformAggregations() {
        ObjectNode aggregationsRequest  = null;
        for(Facet aggregation : aggregations) {
            JsonNode agg = createAgg(aggregation, false);
            aggregationsRequest = addAgg(aggregationsRequest, agg);
        }

        for(Slider slider : sliders) {
            JsonNode agg = createSlider(slider);
            aggregationsRequest = addAgg(aggregationsRequest, agg);
        }

        if(aggregationsRequest != null) {
            elasticQuery.set("aggs", aggregationsRequest.get("aggs"));
        }

    }

    public void transformParameter() {
        Map<String, String> rawValues = RequestParser.getRequestParameter(pipelineContainer);


        for(Map.Entry<String, String> param :profileParameter.entrySet()) {
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

        Map<String, String> escapedValues = rawValues.entrySet().stream().collect(HashMap::new,
                (m,e)->m.put(e.getKey() + ".escaped", escapeValue(e.getValue())), HashMap::putAll);

        Map<String, String> encodedValues = rawValues.entrySet().stream().collect(HashMap::new,
                (m,e)->m.put(e.getKey() + ".encoded", JsonUtil.encode(e.getValue())), HashMap::putAll);

        Map<String, String> replaceMap = new HashMap<>(escapedValues);
        replaceMap.putAll(encodedValues);
        replaceMap.putAll(rawValues);




        String request = "";
        try {
            request = loadProfile(profile, replaceMap);
            elasticQuery = (ObjectNode) getObjectMapper().readTree(request);
        }
        catch (Exception e) {
            if(pipelineContainer.isDebugEnabled()) {
                pipelineContainer.debug("profile", DebugType.STRING, request);
                pipelineContainer.debug("replaceMap", DebugType.OBJECT, replaceMap);
            }
            throw new RuntimeException("Could not load elastic query from profile: " + profile, e);
        }

    }


    public String escapeValue(String value) {
        String escapedValue = ElasticUtil.escape(value);
        String encodedValue = JsonUtil.encode(escapedValue);
        return encodedValue;
    }

    public Map<String, String> encodeValues(Map<String, String> replaceMap) {
        Map<String, String> ret = new HashMap<>();
        for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
            String escapedValue = ElasticUtil.escape(entry.getValue());
            String encodedValue = JsonUtil.encode(escapedValue);
            ret.put(entry.getKey(), encodedValue);
        }
        return ret;
    }

    public StringBuilder print(String indent) {
        StringBuilder printer = new StringBuilder();
        PrintUtil.printKeyValue(printer, indent, "profile", profile);
        return printer;
    }


    protected String loadProfile(String filename, Map<String, String> vars) throws IOException {
        String profile = ProfileLoader.loadProfile(filename, vars);
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            if(entry.getValue() == null) {
                entry.setValue("null");
            }
        }

        StrSubstitutor strSubstitutor = new StrSubstitutor(vars);
        profile = strSubstitutor.replace(profile);
        return profile;
    }



    private ObjectNode addAgg(ObjectNode aggregations, JsonNode agg) {
        if(aggregations == null) {
            aggregations = (ObjectNode) objectMapper.createObjectNode().set("aggs", objectMapper.createObjectNode());
        }
        ((ObjectNode)aggregations.get("aggs")).setAll((ObjectNode) agg);
        return aggregations;
    }


    private JsonNode createSlider(Slider slider) {
        ObjectNode aggField = objectMapper.createObjectNode().
                put("field", slider.getId());
        JsonNode type =
                objectMapper.createObjectNode().set(
                        slider.getType(),aggField
                );
        String name = slider.getName();
        JsonNode agg =
                objectMapper.createObjectNode().set(
                        name,type
                );
        return agg;
    }


    private JsonNode createAgg(Facet facet, boolean isSubFacet) {
        ObjectNode aggField = objectMapper.createObjectNode().
                put("field", facet.getId());

        if(facet.getInclude() != null) {
            aggField.put("include", facet.getInclude());
        }

        if(facet.getExclude() != null) {
            aggField.put("exclude", facet.getExclude());
        }

        if(facet.getSortBy() != null) {
            aggField.set("order", objectMapper.createObjectNode().put(facet.getSortBy(), facet.getSortOrder()));
        }

        if(facet.getSize() != null) {
            aggField.put("size", facet.getSize());
        }
        JsonNode type =
                objectMapper.createObjectNode().set(
                        facet.getType(),aggField
                );

        ObjectNode aggs = (ObjectNode) type;

        if(facet.getSubFacet() != null) {
            JsonNode subAggs = createAgg(facet.getSubFacet(), true);
            aggs.set("aggs", subAggs);
        }


        String name = facet.getName();
        if(isSubFacet) {
            name = "subFacet";
        }
        JsonNode agg =
                objectMapper.createObjectNode().set(
                        name,type
                );
        return agg;
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

    public void setProfileParameter(String key, String value) {
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
}
