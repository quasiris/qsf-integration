package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientIF;
import com.quasiris.qsf.pipeline.filter.elastic.client.StandardElasticClient;
import com.quasiris.qsf.response.SearchResult;
import com.quasiris.qsf.util.PrintUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mki on 19.11.17.
 */
public class ElasticFilter extends AbstractFilter {

    private static Logger LOG = LoggerFactory.getLogger(ElasticFilter.class);

    private String elasticBaseUrl;

    private String resultSetId;

    ElasticClientIF elasticClient = new StandardElasticClient();

    private QueryTransformerIF queryTransformer;

    private SearchResultTransformerIF searchResultTransformer;

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        ObjectNode elasticQuery = queryTransformer.transform(pipelineContainer);
        if(pipelineContainer.isDebugEnabled()) {
            pipelineContainer.debug(elasticQuery);
        }
        ElasticResult elasticResult = elasticClient.request(elasticBaseUrl + "/_search", elasticQuery);
        pipelineContainer.debug(elasticResult);
        SearchResult searchResult = searchResultTransformer.transform(elasticResult);
        pipelineContainer.putSearchResult(resultSetId,searchResult);

        return pipelineContainer;
    }


    @Override
    public StringBuilder print(String indent) {
        StringBuilder printer =  super.print(indent);
        PrintUtil.printKeyValue(printer, indent, "elasticBaseUrl", getElasticBaseUrl());
        PrintUtil.printKeyValue(printer, indent, "resultSetId", getResultSetId());
        return printer;
    }

    public String getElasticBaseUrl() {
        return elasticBaseUrl;
    }

    public void setElasticBaseUrl(String elasticBaseUrl) {
        this.elasticBaseUrl = elasticBaseUrl;
    }

    public String getResultSetId() {
        return resultSetId;
    }

    public void setResultSetId(String resultSetId) {
        this.resultSetId = resultSetId;
    }

    public void setElasticClient(ElasticClientIF elasticClient) {
        this.elasticClient = elasticClient;
    }

    public void setQueryTransformer(QueryTransformerIF queryTransformer) {
        this.queryTransformer = queryTransformer;
    }

    public void setSearchResultTransformer(SearchResultTransformerIF searchResultTransformer) {
        this.searchResultTransformer = searchResultTransformer;
    }
}
