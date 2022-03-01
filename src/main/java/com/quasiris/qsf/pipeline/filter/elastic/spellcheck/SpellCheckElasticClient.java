package com.quasiris.qsf.pipeline.filter.elastic.spellcheck;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.commons.util.JsonUtil;
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
import java.util.List;

/**
 * Created by tbl on 27.2.22.
 */
public class SpellCheckElasticClient {

    private MultiElasticClientIF elasticClient;

    private String baseUrl;

    private int minTokenLenght = 4;
    private int minTokenWeight = 1;

    public SpellCheckElasticClient(String baseUrl, int minTokenLenght, int minTokenWeight) {
        this.baseUrl = baseUrl;
        this.minTokenLenght = minTokenLenght;
        this.minTokenWeight = minTokenWeight;

        if(elasticClient == null) {
            elasticClient = ElasticClientFactory.getMulitElasticClient();
        }
    }

    public List<SpellCheckToken> spellspeck(SearchQuery searchQuery) throws IOException, PipelineContainerException {
        List<String> elasticQueries = new ArrayList<>();
        List<SpellCheckToken> spellCheckTokens = new ArrayList<>();
        for(Token token: searchQuery.getQueryToken()) {
            SpellCheckToken spellCheckToken = new SpellCheckToken(token);
            spellCheckTokens.add(spellCheckToken);

            if(token.getValue().length() < minTokenLenght) {
                continue;
            }

            String elasticRequest  = "{\"query\": {\"bool\": { \"must\": [ {\"fuzzy\": {\"variants.spell\": {\"value\": \""+JsonUtil.encode(token.getValue().toLowerCase())+"\",\"fuzziness\": \"AUTO\"}}},{\"range\": {\"weight\": {\"gte\": "+getMinTokenWeight()+"}}}]}}}";
            elasticQueries.add(elasticRequest);
            spellCheckToken.setElasticResultPojnter(elasticQueries.size()-1);
        }

        if(elasticQueries.isEmpty()) {
            return spellCheckTokens;
        }

        MultiElasticResult multiElasticResult = elasticClient.request(baseUrl + "/_msearch", elasticQueries);

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
            token.setType(SpellCheckTokenType.UNKNOWN);
            return;
        }



        for(Hit hit: elasticResult.getHits().getHits()) {
            String text = getAsText(hit.get_source(), "text");
            if(SpellcheckUtils.fuzzyEquals(token.getToken().getValue(), text)) {
                token.setType(SpellCheckTokenType.CORRECT);
                return;
            }
            Double score = hit.get_score();
            String weightString = getAsText(hit.get_source(), "weight");
            Double weight = Double.valueOf(weightString);
            if(weight > 10) {
                score = score + 100;
            }

            scores.add(new Score(text, score));
        }

        token.setCorrectedVariants(scores);
        token.setType(SpellCheckTokenType.CORRECTED);
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

    public void setElasticClient(MultiElasticClientIF elasticClient) {
        this.elasticClient = elasticClient;
    }
}
