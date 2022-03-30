package com.quasiris.qsf.pipeline.filter.solr;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.SearchResult;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mki on 25.02.18.
 */
public class JsonMonitoringFilter extends AbstractFilter {

    private String url;

    private List<Pair<String, String>> conditions = new ArrayList<>();

    private CloseableHttpClient httpclient = HttpClientBuilder.create().build();

    private String username;

    private String password;

    private String description;
    private String documentationUrl;

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        SearchResult searchResult = new SearchResult();
        int httpCode = 0;
        int aggregatedStatus = 200;
        try {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("graal.js");
            if(engine == null) {
                throw new RuntimeException("Could not load script engine");
            }

            HttpGet httpGet = new HttpGet(url);

            httpGet.setHeader("Content-Type", ContentType.APPLICATION_JSON.toString());

            if(this.username != null && this.password != null) {
                String auth = username + ":" + password;
                byte[] encodedAuth = Base64.encodeBase64(
                        auth.getBytes(Charset.forName("ISO-8859-1")));
                String authHeader = "Basic " + new String(encodedAuth);
                httpGet.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

            }
            CloseableHttpResponse response = httpclient.execute(httpGet);
            httpCode = response.getCode();
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
                document.getDocument().put("description", description);
                document.getDocument().put("documentationUrl", documentationUrl);
                document.getDocument().put("duration", getCurrentTime());
                document.getDocument().put("httpCode", httpCode);



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
            document.getDocument().put("description", description);
            document.getDocument().put("documentationUrl", documentationUrl);
            document.getDocument().put("duration", getCurrentTime());
            document.getDocument().put("httpCode", httpCode);
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

    public void setAuthentication(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }
}
