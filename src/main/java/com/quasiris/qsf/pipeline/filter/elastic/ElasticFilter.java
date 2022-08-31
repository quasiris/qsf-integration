package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.commons.elasticsearch.client.ElasticSearchClient;
import com.quasiris.qsf.commons.util.JsonUtil;
import com.quasiris.qsf.commons.util.UrlUtil;
import com.quasiris.qsf.dto.response.SearchResult;
import com.quasiris.qsf.exception.DebugType;
import com.quasiris.qsf.explain.ExplainContextHolder;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientFactory;
import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientIF;
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
    private ElasticSearchClient elasticSearchClient;

    private QueryTransformerIF queryTransformer;

    private SearchResultTransformerIF searchResultTransformer;

    private ObjectNode elasticQuery;

    @Override
    public void init() {
        super.init();
        if(elasticClient == null && elasticSearchClient == null) {
            elasticSearchClient = ElasticClientFactory.getElasticSearchClient();
        }
    }

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        elasticQuery = queryTransformer.transform(pipelineContainer);
        if(elasticQuery == null) {
            return pipelineContainer;
        }
        ExplainContextHolder.getContext().explain(getId() + ".baseUrl", UrlUtil.removePassword(baseUrl));
        ExplainContextHolder.getContext().explainJson(getId() + ".query", elasticQuery);

        pipelineContainer.debug(getId() + ".baseUrl", DebugType.STRING, UrlUtil.removePassword(baseUrl));
        pipelineContainer.debug(getId() + ".query", DebugType.JSON, elasticQuery);

        ElasticResult elasticResult = null;
        if(elasticSearchClient != null) {
            String jsonQuery = JsonUtil.toJson(elasticQuery);
            elasticResult = elasticSearchClient.search(baseUrl, jsonQuery);
        } else {
            elasticResult = elasticClient.request(baseUrl + "/_search", elasticQuery);
        }

        ExplainContextHolder.getContext().explainJson(getId() + ".result", elasticResult);
        pipelineContainer.debug(getId() + ".result", DebugType.OBJECT, elasticResult);

        searchResultTransformer.init(pipelineContainer);
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

    @Deprecated
    public void setElasticClient(ElasticClientIF elasticClient) {
        this.elasticClient = elasticClient;
    }

    public void setElasticSearchClient(ElasticSearchClient elasticClient) {
        this.elasticSearchClient = elasticClient;
    }

    public ElasticSearchClient getElasticSearchClient() {
        return elasticSearchClient;
    }

    public QueryTransformerIF getQueryTransformer() {
        return queryTransformer;
    }

    public void setQueryTransformer(QueryTransformerIF queryTransformer) {
        this.queryTransformer = queryTransformer;
    }

    public SearchResultTransformerIF getSearchResultTransformer() {
        return searchResultTransformer;
    }

    public void setSearchResultTransformer(SearchResultTransformerIF searchResultTransformer) {
        this.searchResultTransformer = searchResultTransformer;
    }

    @Override
    public PipelineContainer onError(PipelineContainer pipelineContainer, Exception e) {
        String query = "";
        try {
            if(elasticQuery != null) {
                query = JsonBuilder.create().newJson(elasticQuery).writeAsString();
            }
        } catch (Exception ex) {
            query = ex.getMessage();
        }
        LOG.error("The filter: {} failed with an error: {} for query: {}", getId(), e.getMessage(), query, e);
        return super.onError(pipelineContainer, e);
    }
}
