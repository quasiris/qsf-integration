package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Hit;
import com.quasiris.qsf.response.Document;
import com.quasiris.qsf.response.SearchResult;

/**
 * Created by mki on 04.02.18.
 */
public interface SearchResultTransformerIF {

    SearchResult transform(ElasticResult elasticResult);

    Document transformHit(Hit hit);

    StringBuilder print(String indent);
}
