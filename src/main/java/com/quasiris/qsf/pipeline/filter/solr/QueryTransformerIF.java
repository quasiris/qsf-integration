package com.quasiris.qsf.pipeline.filter.solr;

import com.quasiris.qsf.pipeline.PipelineContainer;
import org.apache.solr.client.solrj.SolrQuery;

/**
 * Created by mki on 21.11.16.
 */
public interface QueryTransformerIF {


    SolrQuery transform(PipelineContainer pipelineContainer);

    StringBuilder print(String indent);
}
