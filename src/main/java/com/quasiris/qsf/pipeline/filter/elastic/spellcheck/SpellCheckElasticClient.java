package com.quasiris.qsf.pipeline.filter.elastic.spellcheck;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.commons.elasticsearch.client.ElasticSearchClient;
import com.quasiris.qsf.commons.exception.ResourceNotFoundException;
import com.quasiris.qsf.commons.text.TextUtils;
import com.quasiris.qsf.commons.util.JsonUtil;
import com.quasiris.qsf.explain.ExplainContextHolder;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.pipeline.filter.elastic.Score;
import com.quasiris.qsf.pipeline.filter.elastic.SpellCheckToken;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Hit;
import com.quasiris.qsf.pipeline.filter.elastic.bean.MultiElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientFactory;
import com.quasiris.qsf.pipeline.filter.elastic.client.MultiElasticClientIF;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.Token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tbl on 27.2.22.
 */
public class SpellCheckElasticClient {

    private MultiElasticClientIF elasticClient;
    private ElasticSearchClient elasticSearchClient;

    private String baseUrl;

    private int minTokenLenght = 4;
    private int minTokenWeight = 1;

    public SpellCheckElasticClient(String baseUrl, int minTokenLenght, int minTokenWeight) {
        this.baseUrl = baseUrl;
        this.minTokenLenght = minTokenLenght;
        this.minTokenWeight = minTokenWeight;
        this.elasticSearchClient = ElasticClientFactory.getElasticSearchClient();
    }

    public List<SpellCheckToken> spellspeck(SearchQuery searchQuery) throws IOException, PipelineContainerException {
        List<String> elasticQueries = new ArrayList<>();
        List<SpellCheckToken> spellCheckTokens = new ArrayList<>();
        for(Token token: searchQuery.getQueryToken()) {
            SpellCheckToken spellCheckToken = new SpellCheckToken(token);
            spellCheckTokens.add(spellCheckToken);

            if(token.getValue().length() < minTokenLenght) {
                spellCheckToken.setTypes(Arrays.asList(SpellCheckTokenType.IGNORED));
                continue;
            }

            if(TextUtils.containsNumber(token.getValue())) {
                spellCheckToken.setTypes(Arrays.asList(SpellCheckTokenType.IGNORED));
                continue;
            }

            String elasticRequest  = "{\"query\": {\"bool\": { \"must\": [ {\"fuzzy\": {\"variants.spell\": {\"value\": \""+JsonUtil.encode(token.getValue().toLowerCase())+"\",\"fuzziness\": \"AUTO\"}}},{\"range\": {\"weight\": {\"gte\": "+getMinTokenWeight()+"}}}]}},\"sort\": [\"_score\", {\"weight\": \"desc\"}\t]}";
            elasticQueries.add(elasticRequest);
            spellCheckToken.setElasticResultPojnter(elasticQueries.size()-1);
        }

        if(elasticQueries.isEmpty()) {
            return spellCheckTokens;
        }
        ExplainContextHolder.getContext().explainJson("spellcheckElasticRequest", elasticQueries);

        String requestUrl = baseUrl + "/_msearch";
        ExplainContextHolder.getContext().explain("spellcheckElasticUrl", requestUrl);

        MultiElasticResult multiElasticResult = null;
        if(elasticSearchClient != null) {
            try {
                multiElasticResult = elasticSearchClient.multiSearch(baseUrl, elasticQueries);
            } catch (ResourceNotFoundException e) {
                throw new PipelineContainerException("Error during elastic request!", e);
            }
        } else {
            multiElasticResult = elasticClient.request(requestUrl, elasticQueries);
        }
        ExplainContextHolder.getContext().explainJson("spellcheckElasticResult", multiElasticResult);

        for (SpellCheckToken spellCheckToken : spellCheckTokens) {
            if(spellCheckToken.getElasticResultPojnter() != null) {
                computeScoresFromElastic(spellCheckToken, multiElasticResult.getResponses().get(spellCheckToken.getElasticResultPojnter()));
            }
        }
        return spellCheckTokens;
    }

    void computeScoresFromElastic(SpellCheckToken token, ElasticResult elasticResult) {
        List<Score> scores = new ArrayList<>();
        if(elasticResult.getHits() == null || elasticResult.getHits().getHits().isEmpty() ) {
            token.setTypes(Arrays.asList(SpellCheckTokenType.UNKNOWN));
            return;
        }



        for(Hit hit: elasticResult.getHits().getHits()) {
            String text = getAsText(hit.get_source(), "text");
            List<String> types = getAsList(hit.get_source(), "type");
            if(token.getTypes() == null) {
                token.setTypes(SpellCheckTokenType.creates(types));
            }

            if(SpellcheckUtils.fuzzyEquals(token.getToken().getValue(), text)) {
                token.setTypes(Arrays.asList(SpellCheckTokenType.EQUALS));
                return;
            }
            Double score = hit.get_score();
            String weightString = getAsText(hit.get_source(), "weight");
            Double weight = Double.valueOf(weightString);
            if(weight > 10) {
                score = score + 100;
            }
            Score s = new Score(text, score);
            s.setTypes(SpellCheckTokenType.creates(types));
            scores.add(s);
        }

        token.setCorrectedVariants(scores);
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

    private List<String> getAsList(ObjectNode objectNode, String name) {
        List<String> ret = new ArrayList<>();
        JsonNode node = objectNode.get(name);
        if(node == null) {
            return ret;
        }
        if(node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                ret.add(node.get(i).asText());
            }


        } else {
            ret.add(node.asText());
        }
        return ret;

    }

    public int getMinTokenLenght() {
        return minTokenLenght;
    }

    public void setMinTokenLenght(int minTokenLenght) {
        this.minTokenLenght = minTokenLenght;
    }

    public int getMinTokenWeight() {
        return minTokenWeight;
    }

    public void setMinTokenWeight(int minTokenWeight) {
        this.minTokenWeight = minTokenWeight;
    }

    @Deprecated
    public void setElasticClient(MultiElasticClientIF elasticClient) {
        this.elasticClient = elasticClient;
    }

    public void setElasticSearchClient(ElasticSearchClient elasticSearchClient) {
        this.elasticSearchClient = elasticSearchClient;
    }
}
