package com.quasiris.qsf.pipeline.filter.elastic.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Analyze;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;

import java.io.IOException;

/**
 * Created by mki on 16.12.17.
 */
@Deprecated
public interface ElasticClientIF {

    Analyze analyze(String elasticBaseUrl, String request) throws IOException;

    ElasticResult request(String elasticBaseUrl, String request) throws IOException;

    ElasticResult request(String elasticBaseUrl, JsonNode request) throws IOException;

    void index(String elasticBaseUrl, String request) throws IOException;

    void index(String elasticBaseUrl, Object request) throws IOException;
}
