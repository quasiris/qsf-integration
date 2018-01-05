package de.quasiris.qsf.pipeline.filter.elastic.client;

import de.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;

import java.io.IOException;

/**
 * Created by mki on 16.12.17.
 */
public interface ElasticClientIF {

    ElasticResult request(String elasticBaseUrl, String request) throws IOException;
}
