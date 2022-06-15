package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.commons.elasticsearch.client.ElasticSearchClient;
import com.quasiris.qsf.commons.text.TextUtils;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Hit;
import com.quasiris.qsf.pipeline.filter.elastic.bean.MultiElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientFactory;
import com.quasiris.qsf.pipeline.filter.elastic.client.MultiElasticClientIF;
import com.quasiris.qsf.query.PosTag;
import com.quasiris.qsf.query.Token;
import com.quasiris.qsf.commons.util.JsonUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tbl on 22.12.18.
 */
public class PosTaggerElasticFilter extends AbstractFilter {

    private static Logger LOG = LoggerFactory.getLogger(PosTaggerElasticFilter.class);

    private String baseUrl = "http://localhost:9200/pos-tag";

    private MultiElasticClientIF elasticClient;
    private ElasticSearchClient elasticSearchClient;

    @Override
    public void init() {
        super.init();
        if(elasticClient == null && elasticSearchClient == null) {
            elasticSearchClient = ElasticClientFactory.getElasticSearchClient();
        }
    }

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        List<String> elasticQueries = new ArrayList<>();
        for(Token token: pipelineContainer.getSearchQuery().getQueryToken()) {

            String elasticRequest  = "{\"query\": {\"match\" : {\"token.stemmed\" : \""+ token + "\"}}}";
            elasticRequest  = "{\"query\":{\"dis_max\": {\"queries\": [{\"query_string\": {\"_name\": \"dismax\",\"query\": \"" + JsonUtil.encode(token.getValue()) + "\",\"default_operator\": \"AND\",\"fields\": [\"token.stemmed^1\", \"token.exact^200\"]}}]}}}";
            elasticQueries.add(elasticRequest);
        }

        //System.out.println("b: " + pipelineContainer.currentTime());

        MultiElasticResult multiElasticResult = null;
        if(elasticSearchClient != null) {
            multiElasticResult = elasticSearchClient.multiSearch(baseUrl, elasticQueries);
        } else {
            multiElasticResult = elasticClient.request(baseUrl + "/_msearch", elasticQueries);
        }

        //System.out.println("a: " + pipelineContainer.currentTime());

        if(multiElasticResult.getResponses().size() != pipelineContainer.getSearchQuery().getQueryToken().size()) {
            throw new RuntimeException("something get wrong");
        }

        for (int i = 0; i <multiElasticResult.getResponses().size() ; i++) {
            Hit hit = multiElasticResult.getResponses().get(i).getHits().getHits().stream().findFirst().orElse(null);
            String postag = null;
            String attrName = "unknown";
            if(hit != null) {
                postag = getAsText(hit.get_source(), "postag");
                attrName = getAsText(hit.get_source(), "attributeName");
            }



            if(postag == null) {
                //do nothing
            } else if(NumberUtils.isCreatable(pipelineContainer.getSearchQuery().getQueryToken().get(i).getValue())) {
                postag = "<NUM>";
            } else if( postag.equals("<PRODUCT>")) {
                // if we know that the token is a product we do nothing else
            } else if( postag.equals("<BRAND>")) {
                // if we know that the token is a brand we do nothing else
            } else if( postag.equals("<UNIT>")) {
                // if we know that the token is a unit we do nothing else
            } else if(pipelineContainer.getSearchQuery().getQueryToken().get(i).getValue().length() < 2) {
                postag = "<SYM>";
            } else if(TextUtils.containsNumber(pipelineContainer.getSearchQuery().getQueryToken().get(i).getValue())) {
                postag = "<SYM>";
            }




            Token token = pipelineContainer.getSearchQuery().getQueryToken().get(i);
            if(postag == null) {
                // ignore
            } else if(PosTag.isOneOfValue(postag, PosTag.BETWEEN, PosTag.GREATER, PosTag.LESS)) {
                token.setPosTag(postag);
            } else if(PosTag.isOneOfValue(token.getPosTag(), PosTag.APPR, PosTag.VAFIN, PosTag.PIDAT, PosTag.ART)) {
                // ignore
            } else {
                token.setPosTag(postag);
            }
            token.setAttributeName(attrName);
            overridePostag(token);
        }

        pipelineContainer.putContext("elasticPosTagger", pipelineContainer.getSearchQuery().copyQueryToken());

        return pipelineContainer;
    }

    private static Map<String, String> posTagOverrides = new HashMap<>();
    static {
        posTagOverrides.put("max", "<SYM>");
        posTagOverrides.put("pro", "<SYM>");
        posTagOverrides.put("plus", "<SYM>");
        posTagOverrides.put("als", "<IGNORE>");
    }

    private Token overridePostag(Token token) {
        String key = token.getNormalizedValue().toLowerCase();
        String value = posTagOverrides.get(key);
        if(value != null) {
            token.setPosTag(value);
        }

        return token;
    }

    private String getAsText(ObjectNode objectNode, String name) {
        JsonNode node = objectNode.get(name);
        if(node == null) {
            return null;
        }
        if(node.isArray()) {
            return node.get(0).asText();
        }
        return node.asText();

    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
