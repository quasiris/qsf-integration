package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.exception.DebugType;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientFactory;
import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientIF;
import com.quasiris.qsf.response.SearchResult;
import com.quasiris.qsf.util.PrintUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mki on 19.11.17.
 */
public class ElasticFilter extends AbstractFilter {

    private static Logger LOG = LoggerFactory.getLogger(ElasticFilter.class);

    private String baseUrl;

    private String resultSetId;

    private ElasticClientIF elasticClient;

    private QueryTransformerIF queryTransformer;

    private SearchResultTransformerIF searchResultTransformer;

    @Override
    public void init() {
        super.init();
        if(elasticClient == null) {
            elasticClient = ElasticClientFactory.getElasticClient();
        }
    }

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        ObjectNode elasticQuery = queryTransformer.transform(pipelineContainer);

        pipelineContainer.debug(getId() + ".baseUrl", DebugType.STRING, baseUrl);
        pipelineContainer.debug(getId() + ".query", DebugType.JSON, elasticQuery);

        ElasticResult elasticResult = elasticClient.request(baseUrl + "/_search", elasticQuery);
        pipelineContainer.debug(getId() + ".result", DebugType.OBJECT, elasticResult);
        SearchResult searchResult = searchResultTransformer.transform(elasticResult);
        searchResult.setName(resultSetId);
        pipelineContainer.putSearchResult(resultSetId,searchResult);

        return pipelineContainer;
    }

    @Override
    public StringBuilder print(String indent) {
        StringBuilder printer =  super.print(indent);
        PrintUtil.printKeyValue(printer, indent, "baseUrl", getBaseUrl());
        PrintUtil.printKeyValue(printer, indent, "resultSetId", getResultSetId());
        return printer;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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

    @Override
    public PipelineContainer onError(PipelineContainer pipelineContainer, Exception e) {
        LOG.error("The filter: " + getId() + " failed with an error: " + e.getMessage());
        return super.onError(pipelineContainer, e);
    }
}
