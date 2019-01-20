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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by mki on 04.02.17.
 */
public class ElasticParameterQueryTransformer implements QueryTransformerIF {

    private PipelineContainer pipelineContainer;

    private SearchQuery searchQuery;

    private ObjectNode elasticQuery;

    private String profile;

    private List<Facet> aggregations = new ArrayList<>();

    private ObjectMapper objectMapper = new ObjectMapper();



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
            aggregationsRequest = createAgg(aggregationsRequest, aggregation);
        }
        if(aggregationsRequest != null) {
            elasticQuery.set("aggs", aggregationsRequest.get("aggs"));
        }

    }

    public void transformParameter() {
        Map<String, String> replaceMap = RequestParser.getRequestParameter(pipelineContainer);
        for(Map.Entry<String, String> param :pipelineContainer.getParameters().entrySet()) {
            replaceMap.put("param." + param.getKey(), param.getValue());
        }

        replaceMap = encodeValues(replaceMap);

        try {
            String request = loadProfile(profile, replaceMap);
            elasticQuery = (ObjectNode) getObjectMapper().readTree(request);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not load elastic query from profile: " + profile, e);
        }

    }

    public Map<String, String> encodeValues(Map<String, String> replaceMap) {
        Map<String, String> ret = new HashMap<>();
        for(Map.Entry<String, String> entry : replaceMap.entrySet()) {
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


    private String loadProfile(String filename, Map<String, String> vars) throws IOException {
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


    private ObjectNode createAgg(ObjectNode aggregations, Facet facet) {
        if(aggregations == null) {
            aggregations = (ObjectNode) objectMapper.createObjectNode().set("aggs", objectMapper.createObjectNode());
        }


        ObjectNode aggField = objectMapper.createObjectNode().
                put("field", facet.getId());

        if(facet.getSortBy() != null) {
            aggField.set("order", objectMapper.createObjectNode().put(facet.getSortBy(), facet.getSortOrder()));
        }

        if(facet.getSize() != null) {
            aggField.put("size", facet.getSize());
        }
        JsonNode agg =
                objectMapper.createObjectNode().set(
                        facet.getType(),aggField

                );

        ((ObjectNode)aggregations.get("aggs")).set(facet.getName(), agg);
        return aggregations;
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

    public ObjectNode getElasticQuery() {
        return elasticQuery;
    }

    public void addAggregation(String name, String field) {
        Facet facet = new Facet();
        facet.setName(name);
        facet.setId(field);
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
