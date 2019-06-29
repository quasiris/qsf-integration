package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.pipeline.filter.web.RequestParser;
import com.quasiris.qsf.query.Facet;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.util.ElasticUtil;
import com.quasiris.qsf.util.JsonUtil;
import com.quasiris.qsf.util.PrintUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    protected ObjectMapper objectMapper = new ObjectMapper();



    @Override
    public ObjectNode transform(PipelineContainer pipelineContainer) throws PipelineContainerException {
        this.pipelineContainer = pipelineContainer;
        this.searchQuery = pipelineContainer.getSearchQuery();

        transformParameter();
        transformAggregations();

        return elasticQuery;
    }


    public void transformAggregations() {
        ObjectNode aggregationsRequest  = null;
        for(Facet aggregation : aggregations) {
            aggregationsRequest = addAgg(aggregationsRequest, aggregation);
        }
        if(aggregationsRequest != null) {
            elasticQuery.set("aggs", aggregationsRequest.get("aggs"));
        }

    }

    public void transformParameter() {
        Map<String, String> replaceMap = RequestParser.getRequestParameter(pipelineContainer);


        for(Map.Entry<String, String> param :profileParameter.entrySet()) {
            replaceMap.put("profile." + param.getKey(), param.getValue());
        }

        if(searchQuery != null) {
            if(searchQuery.getQ() != null) {
                replaceMap.put("qsfql.q", encodeValue(searchQuery.getQ()));
            }
            replaceMap.putAll(encodeValues(searchQuery.getParametersWithPrefix("qsfql")));
        }

        for(Map.Entry<String, String> param :pipelineContainer.getParameters().entrySet()) {
            replaceMap.put("param." + param.getKey(), encodeValue(param.getValue()));
        }


        try {
            String request = loadProfile(profile, replaceMap);
            elasticQuery = (ObjectNode) getObjectMapper().readTree(request);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not load elastic query from profile: " + profile, e);
        }

    }


    public String encodeValue(String value) {
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
        String profile = null;
        if (filename.startsWith("classpath://")) {
            profile = loadProfileFromClasspath(filename);
        } else {
            profile = loadProfileFromFile(filename);
        }


        StrSubstitutor strSubstitutor = new StrSubstitutor(vars);
        profile = strSubstitutor.replace(profile);
        return profile;
    }

    private String loadProfileFromFile(String filename) throws IOException {
        File file = new File(filename);
        String profile = Files.toString(file, Charsets.UTF_8);
        return profile;
    }

    private String loadProfileFromClasspath(String filename) throws IOException {

        String resource = filename.replaceFirst("classpath://", "");
        InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream(resource);

        String profile = IOUtils.toString(in, Charset.forName("UTF-8"));
        IOUtils.closeQuietly(in);
        return profile;


    }


    private ObjectNode addAgg(ObjectNode aggregations, Facet facet) {
        if(aggregations == null) {
            aggregations = (ObjectNode) objectMapper.createObjectNode().set("aggs", objectMapper.createObjectNode());
        }


        JsonNode agg = createAgg(facet, false);

        ((ObjectNode)aggregations.get("aggs")).setAll((ObjectNode) agg);
        return aggregations;
    }


    private JsonNode createAgg(Facet facet, boolean isSubFacet) {
        ObjectNode aggField = objectMapper.createObjectNode().
                put("field", facet.getId());

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
}
