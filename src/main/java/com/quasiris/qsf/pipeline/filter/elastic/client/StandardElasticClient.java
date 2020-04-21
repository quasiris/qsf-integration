package com.quasiris.qsf.pipeline.filter.elastic.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Analyze;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by mki on 16.12.17.
 */
public class StandardElasticClient implements  ElasticClientIF {
    private static Logger LOG = LoggerFactory.getLogger(StandardElasticClient.class);

    private ObjectMapper objectMapper;

    public StandardElasticClient() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    }

    @Override
    public Analyze analyze(String elasticBaseUrl, String request) throws IOException {
        LOG.debug("elastic url: {} request: {}", elasticBaseUrl, request);
        String response = ElasticHttpClient.post(elasticBaseUrl, request);
        Analyze analyze = objectMapper.readValue(response, Analyze.class);
        return analyze;
    }

    public ElasticResult request(String elasticUrl, String request) throws IOException {
        LOG.debug("elastic url: {} request: {}", elasticUrl, request);
        String response = ElasticHttpClient.post(elasticUrl, request);
        ElasticResult elasticResult = objectMapper.readValue(response, ElasticResult.class);
        return elasticResult;
    }

    @Override
    public ElasticResult request(String elasticBaseUrl, JsonNode jsonNode) throws IOException {
        String request = objectMapper.writeValueAsString(jsonNode);
        return request(elasticBaseUrl, request);
    }

    @Override
    public void index(String elasticBaseUrl, String request) throws IOException {
        LOG.debug("elastic url: {} request: {}", elasticBaseUrl, request);
        ElasticHttpClient.postAsync(elasticBaseUrl, request);
    }

    @Override
    public void index(String elasticBaseUrl, Object request) throws IOException {
        String requestAsString = objectMapper.writeValueAsString(request);
        index(elasticBaseUrl, requestAsString);
    }
}
