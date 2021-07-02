package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.exception.DebugType;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Analyze;
import com.quasiris.qsf.pipeline.filter.elastic.bean.AnalyzeToken;
import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientFactory;
import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientIF;
import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.SearchResult;
import com.quasiris.qsf.util.PrintUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tbl on 21.04.20.
 */
public class ElasticAnalyzeFilter extends AbstractFilter {

    private static Logger LOG = LoggerFactory.getLogger(ElasticAnalyzeFilter.class);

    private String baseUrl;

    private String resultSetId;
    private String field;

    private ElasticClientIF elasticClient;

    @Override
    public void init() {
        super.init();
        if(elasticClient == null) {
            elasticClient = ElasticClientFactory.getElasticClient();
        }
    }

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("field", field);
        parameters.put("query", pipelineContainer.getSearchQuery().getQ());

        parameters = ProfileLoader.encodeParameters(parameters);

        String request = ProfileLoader.loadProfile(Profiles.analyze(), parameters);
        pipelineContainer.debug(getId() + ".baseUrl", DebugType.STRING, baseUrl);
        pipelineContainer.debug(getId() + ".request", DebugType.JSON, request);

        Analyze analyze = elasticClient.analyze(baseUrl + "/_analyze", request);
        pipelineContainer.debug(getId() + ".result", DebugType.OBJECT, analyze);
        SearchResult searchResult = new SearchResult();

        for(AnalyzeToken token : analyze.getTokens()) {
            Document document = new Document();
            document.setValue("token", token.getToken());
            document.setValue("start_offset", token.getStart_offset());
            document.setValue("end_offset", token.getEnd_offset());
            document.setValue("position", token.getPosition());
            document.setValue("type", token.getType());
            document.setValue("positionLength", token.getPositionLength());
            searchResult.addDocument(document);
        }


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

    /**
     * Getter for property 'field'.
     *
     * @return Value for property 'field'.
     */
    public String getField() {
        return field;
    }

    /**
     * Setter for property 'field'.
     *
     * @param field Value to set for property 'field'.
     */
    public void setField(String field) {
        this.field = field;
    }

    @Override
    public PipelineContainer onError(PipelineContainer pipelineContainer, Exception e) {
        LOG.error("The filter: " + getId() + " failed with an error: " + e.getMessage());
        return super.onError(pipelineContainer, e);
    }
}
