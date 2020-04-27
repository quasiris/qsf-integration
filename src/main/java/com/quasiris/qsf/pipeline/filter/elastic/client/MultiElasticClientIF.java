package com.quasiris.qsf.pipeline.filter.elastic.client;

import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.pipeline.filter.elastic.bean.MultiElasticResult;

import java.io.IOException;
import java.util.List;

/**
 * Created by tbl on 22.12.18.
 */
public interface MultiElasticClientIF {

    MultiElasticResult request(String elasticBaseUrl, List<String> request) throws IOException, PipelineContainerException;

}
