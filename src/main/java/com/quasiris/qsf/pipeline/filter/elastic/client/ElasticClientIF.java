package com.quasiris.qsf.pipeline.filter.elastic.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;

import java.io.IOException;

/**
 * Created by mki on 16.12.17.
 */
public interface ElasticClientIF {

    ElasticResult request(String elasticBaseUrl, String request) throws IOException;

    ElasticResult request(String elasticBaseUrl, JsonNode request) throws IOException;
}
