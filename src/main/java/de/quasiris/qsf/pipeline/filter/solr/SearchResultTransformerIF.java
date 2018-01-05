package de.quasiris.qsf.pipeline.filter.solr;

import de.quasiris.qsf.response.Document;
import de.quasiris.qsf.response.SearchResult;
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
