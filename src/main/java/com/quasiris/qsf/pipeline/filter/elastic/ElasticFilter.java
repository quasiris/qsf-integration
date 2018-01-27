package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Hit;
import com.quasiris.qsf.pipeline.filter.elastic.client.StandardElasticClient;
import com.quasiris.qsf.pipeline.filter.web.RequestParser;
import com.quasiris.qsf.response.Document;
import com.quasiris.qsf.response.SearchResult;
import com.quasiris.qsf.util.PrintUtil;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientIF;
import com.quasiris.qsf.util.JsonUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by mki on 19.11.17.
 */
public class ElasticFilter extends AbstractFilter {

    private static Logger LOG = LoggerFactory.getLogger(ElasticFilter.class);

    private String profile;
    private String elasticBaseUrl;

    private String resultSetId;

    ElasticClientIF elasticClient = new StandardElasticClient();

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        Map<String, String> replaceMap = RequestParser.getRequestParameter(pipelineContainer);
        String request = loadProfile(profile, replaceMap);

        if(pipelineContainer.isDebugEnabled()) {
            pipelineContainer.debug(JsonUtil.toJson(request));
        }

        ElasticResult elasticResult = elasticClient.request(elasticBaseUrl + "/_search", request);
        pipelineContainer.debug(elasticResult);

        SearchResult searchResult = new SearchResult();
        searchResult.initDocuments();
        searchResult.setTotal(elasticResult.getHits().getTotal());
        searchResult.setStatusMessage("OK");

        for(Hit hit :elasticResult.getHits().getHits()) {
            ObjectNode objectNode = hit.get_source();
            Iterator<Map.Entry<String, JsonNode>> it = objectNode.fields();
            Document document = new Document();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();

                JsonNode jsonNode = entry.getValue();
                Object value = null;
                if(jsonNode.isTextual()) {
                    value = jsonNode.textValue();
                } else if( jsonNode.isNumber()) {
                    value = jsonNode.numberValue();
                } else {
                    value = jsonNode;
                }
                document.getDocument().put(entry.getKey(), value);
            }

            for(Map.Entry<String, List<String>> entry : hit.getHighlight().entrySet()) {
                document.getDocument().put("highlight." + entry.getKey(), entry.getValue());

            }
            searchResult.addDocument(document);
        }


        pipelineContainer.putSearchResult(resultSetId,searchResult);

        return pipelineContainer;
    }

    private String loadProfileFromFile(String filename) throws IOException {
        File file = new File(filename);
        String profile = Files.toString(file, Charsets.UTF_8);
        return profile;
    }

    private String loadProfileFromClasspath(String filename) throws IOException {

        String resource = filename.replaceFirst("classpath://", "");
        InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream(resource);

        String profile = IOUtils.toString(in, Charset.forName("UTF-8"));
        IOUtils.closeQuietly(in);
        return profile;


    }

    private String loadProfile(String filename, Map<String, String> vars) throws IOException {
        String profile = null;
        if(filename.startsWith("classpath://")) {
            profile = loadProfileFromClasspath(filename);
        } else {
            profile = loadProfileFromFile(filename);
        }


        StrSubstitutor strSubstitutor = new StrSubstitutor(vars);
        profile = strSubstitutor.replace(profile);
        return profile;
    }

    @Override
    public StringBuilder print(String indent) {
        StringBuilder printer =  super.print(indent);
        PrintUtil.printKeyValue(printer, indent, "profile", getProfile());
        PrintUtil.printKeyValue(printer, indent, "elasticBaseUrl", getElasticBaseUrl());
        PrintUtil.printKeyValue(printer, indent, "resultSetId", getResultSetId());
        return printer;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
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
}
