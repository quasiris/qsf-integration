package com.quasiris.qsf.pipeline.filter.elastic.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasiris.qsf.pipeline.filter.elastic.bean.MultiElasticResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by tbl on 22.12.18.
 */
public class StandardMultiElasticClient implements MultiElasticClientIF {
    private static Logger LOG = LoggerFactory.getLogger(StandardMultiElasticClient.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    public MultiElasticResult request(String elasticUrl, List<String> requests) throws IOException {
        LOG.debug("elastic request: " + requests);
        StringBuilder multiRequest = new StringBuilder();
        for(String request: requests) {
            multiRequest.append("{}").append("\n");
            multiRequest.append(request).append("\n");
        }

        String response = ElasticHttpClient.post(elasticUrl, multiRequest.toString(), "application/x-ndjson");
        MultiElasticResult multiElasticResult = objectMapper.readValue(response, MultiElasticResult.class);
        return multiElasticResult;
    }
}
