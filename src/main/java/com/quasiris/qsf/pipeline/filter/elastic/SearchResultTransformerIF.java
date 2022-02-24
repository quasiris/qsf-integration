package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Hit;
import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.SearchResult;

/**
 * Created by mki on 04.02.18.
 */
public interface SearchResultTransformerIF {

    void init(PipelineContainer pipelineContainer);

    SearchResult transform(ElasticResult elasticResult);

    Document transformHit(Hit hit);

    StringBuilder print(String indent);
}
