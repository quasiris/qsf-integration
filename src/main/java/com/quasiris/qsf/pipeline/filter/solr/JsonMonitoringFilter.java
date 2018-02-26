package com.quasiris.qsf.pipeline.filter.solr;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.response.Document;
import com.quasiris.qsf.response.SearchResult;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mki on 25.02.18.
 */
public class JsonMonitoringFilter extends AbstractFilter {

    private String url;

    private List<Pair<String, String>> conditions = new ArrayList<>();

    private CloseableHttpClient httpclient = HttpClients.createDefault();

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {

        SearchResult searchResult = new SearchResult();
        int aggregatedStatus = 200;
        try {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");

            HttpGet httpGet = new HttpGet(url);

            httpGet.setHeader("Content-Type", "application/json");

            CloseableHttpResponse response = httpclient.execute(httpGet);
            StringBuilder responseBuilder = new StringBuilder();
            responseBuilder.append(EntityUtils.toString(response.getEntity()));
            httpclient.close();

            DocumentContext context = context = JsonPath.parse(responseBuilder.toString());

            for (Pair<String, String> condition : conditions) {
                Document document = new Document();
                Object value = context.read(condition.getLeft());
                String stringValue = String.valueOf(value);
                document.getDocument().put("url", url);
                document.getDocument().put("condition", condition.getLeft());
                document.getDocument().put("expected", condition.getRight());
                document.getDocument().put("actual", stringValue);

                String function = "var value =" + stringValue + "; " + condition.getRight() + ";";

                if ((Boolean) engine.eval(function)) {
                    document.getDocument().put("status", "200");
                } else {
                    aggregatedStatus = 500;
                    document.getDocument().put("status", "500");
                }
                searchResult.addDocument(document);
            }

        } catch (Exception e) {
            aggregatedStatus = 500;
            Document document = new Document();
            document.getDocument().put("url", url);
            document.getDocument().put("error", e.getMessage());
            searchResult.addDocument(document);
        }

        searchResult.setStatusCode(aggregatedStatus);
        pipelineContainer.putSearchResult(getId(), searchResult);
        return pipelineContainer;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void addCondition(String jsonPath, String expected) {
        conditions.add(Pair.of(jsonPath, expected));
    }

    public void setHttpclient(CloseableHttpClient httpclient) {
        this.httpclient = httpclient;
    }
}
