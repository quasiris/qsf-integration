package com.quasiris.qsf.pipeline.filter.solr;

import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.SearchResult;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

/**
 * Created by mki on 21.11.16.
 */
public interface SearchResultTransformerIF {

    SearchResult transform(QueryResponse queryResponse);

    Document transformDocument(SolrDocument solrDocument);

    void transformField(Document document, String name, Object value);

    StringBuilder print(String indent);
}
