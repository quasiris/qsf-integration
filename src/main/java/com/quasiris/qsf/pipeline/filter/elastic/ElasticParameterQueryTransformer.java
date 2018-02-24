package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.pipeline.filter.web.RequestParser;
import com.quasiris.qsf.query.SearchQuery;
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

    private List<Pair<String, String>> aggregations = new ArrayList<>();

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
        for(Pair<String, String> aggregation : aggregations) {
            aggregationsRequest = createAgg(aggregationsRequest, aggregation.getLeft(), aggregation.getRight());
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

        try {
            String request = loadProfile(profile, replaceMap);
            elasticQuery = (ObjectNode) getObjectMapper().readTree(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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


    private ObjectNode createAgg(ObjectNode aggregations, String name, String field) {
        if(aggregations == null) {
            aggregations = (ObjectNode) objectMapper.createObjectNode().set("aggs", objectMapper.createObjectNode());
        }

        JsonNode agg =
                objectMapper.createObjectNode().set("terms",
                        objectMapper.createObjectNode().put("field", field));
        ((ObjectNode)aggregations.get("aggs")).set(name, agg);
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
        Pair<String, String> pair = new ImmutablePair<>(name, field);
        aggregations.add(pair);
    }

    public String getProfile() {
        return profile;
    }
}
