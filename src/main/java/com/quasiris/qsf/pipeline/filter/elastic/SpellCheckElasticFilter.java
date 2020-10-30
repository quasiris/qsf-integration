package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.exception.DebugType;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.pipeline.filter.elastic.bean.ElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.bean.Hit;
import com.quasiris.qsf.pipeline.filter.elastic.bean.MultiElasticResult;
import com.quasiris.qsf.pipeline.filter.elastic.client.ElasticClientFactory;
import com.quasiris.qsf.pipeline.filter.elastic.client.MultiElasticClientIF;
import com.quasiris.qsf.pipeline.filter.elastic.client.QSFHttpClient;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.Token;
import com.quasiris.qsf.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by tbl on 11.4.20.
 */
public class SpellCheckElasticFilter extends AbstractFilter {

    private static Logger LOG = LoggerFactory.getLogger(SpellCheckElasticFilter.class);

    private int minTokenLenght = 4;
    private int maxTokenLenght = 10;
    private int minTokenWeight = 1;

    private String baseUrl;

    private MultiElasticClientIF elasticClient;

    private boolean sentenceScoringEnabled = true;

    public SpellCheckElasticFilter(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void init() {
        super.init();
        if(elasticClient == null) {
            elasticClient = ElasticClientFactory.getMulitElasticClient();
        }
    }

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        process(pipelineContainer);
        return pipelineContainer;
    }


    private void process(PipelineContainer pipelineContainer) throws IOException, PipelineContainerException {

        if(pipelineContainer.getSearchQuery().getQueryToken().size() > maxTokenLenght) {
            return;
        }

        List<String> elasticQueries = new ArrayList<>();
        List<SpellCheckToken> spellCheckTokens = new ArrayList<>();
        for(Token token: pipelineContainer.getSearchQuery().getQueryToken()) {
            SpellCheckToken spellCheckToken = new SpellCheckToken(token);
            spellCheckTokens.add(spellCheckToken);

            if(token.getValue().length() < minTokenLenght) {
                continue;
            }

            String elasticRequest  = "{\"query\": {\"bool\": { \"must\": [ {\"fuzzy\": {\"text.keyword\": {\"value\": \""+JsonUtil.encode(token.getValue().toLowerCase())+"\",\"fuzziness\": \"AUTO\"}}},{\"range\": {\"weight\": {\"gte\": "+getMinTokenWeight()+"}}}]}}}";
            elasticQueries.add(elasticRequest);
            spellCheckToken.setElasticResultPojnter(elasticQueries.size()-1);
        }

        if(elasticQueries.isEmpty()) {
            return;
        }

        MultiElasticResult multiElasticResult = elasticClient.request(baseUrl + "/_msearch", elasticQueries);

        for (SpellCheckToken spellCheckToken : spellCheckTokens) {
            if(spellCheckToken.getElasticResultPojnter() != null) {
                computeScoresFromElastic(spellCheckToken, multiElasticResult.getResponses().get(spellCheckToken.getElasticResultPojnter()));
            }
        }

        if(pipelineContainer.isDebugEnabled()) {
            pipelineContainer.debug("spellCheckTokens", DebugType.JSON, spellCheckTokens);
        }

        List<Score> correctedQueryVariants = new ArrayList<>();
        correctedQueryVariants.add(new Score("", 0.0));
        for (SpellCheckToken spellCheckToken : spellCheckTokens) {
            correctedQueryVariants = computeVariants(spellCheckToken, correctedQueryVariants);
        }
        correctedQueryVariants.sort(Comparator.comparing(Score::getScore).reversed());

        if(sentenceScoringEnabled && correctedQueryVariants.size() > 1) {
            // do some bert magic
            List<String> s = correctedQueryVariants.stream().
                    limit(4).
                    map(Score::getText).
                    collect(Collectors.toList());

            Sentences sentences = new Sentences();
            sentences.setSentences(s);


            QSFHttpClient qsfHttpClient = new QSFHttpClient();
            SentencesResponse sentencesResponse = qsfHttpClient.
                    post("http://localhost:5000/v1/sentence-scoring", sentences, SentencesResponse.class);

            correctedQueryVariants = sentencesResponse.getSentences();
            correctedQueryVariants.sort(Comparator.comparing(Score::getScore).reversed());
        }



        Score corrected = correctedQueryVariants.stream().findFirst().orElse(null);
        SearchQuery searchQuery = pipelineContainer.getSearchQuery();
        if(!fuzzyEquals(corrected.getText(), searchQuery.getQ())) {
            searchQuery.setOriginalQuery(searchQuery.getQ());
            searchQuery.setQ(corrected.getText());
            searchQuery.setQueryChanged(true);
        }
    }


    List<Score> computeVariants(SpellCheckToken spellCheckToken, List<Score> spellcheckVariants) {
        List<Score> extendedCorrectedQueryVariants = new ArrayList<>();
        List<Score> correctedVariants = spellCheckToken.getCorrectedVariants();

        if(correctedVariants == null) {
            correctedVariants = new ArrayList<>();
            correctedVariants.add(new Score(spellCheckToken.getToken().getValue(), 0.0));

        }
        for(Score score : correctedVariants) {
            for(Score spellcheckVariant : spellcheckVariants) {
                extendedCorrectedQueryVariants.add(
                        new Score(
                                spellcheckVariant.getText() + " " + score.getText(),
                                spellcheckVariant.getScore() + score.getScore()));
            }
        }

        return extendedCorrectedQueryVariants;



    }

    void computeScoresFromElastic(SpellCheckToken token, ElasticResult elasticResult) {
        List<Score> scores = new ArrayList<>();
        if(elasticResult.getHits() == null || elasticResult.getHits().getHits().isEmpty() ) {
            token.setUnknownToken(true);
            return;
        }



        for(Hit hit: elasticResult.getHits().getHits()) {
            String text = getAsText(hit.get_source(), "text");
            if(fuzzyEquals(token.getToken().getValue(), text)) {
                token.setCorrectToken(true);
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
        token.setUnknownToken(false);
        token.setCorrectToken(false);
    }


    private boolean fuzzyEquals(String left, String right) {
        if(left == null && right == null) {
            return true;
        }
        if(left == null || right == null) {
            return false;
        }
        left = normalize(left);
        right = normalize(right);
        return left.equals(right);
    }

    private String normalize(String value) {
        if(value == null) {
            return null;
        }
        value = value.toLowerCase();
        value = value.trim();
        return value;
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

    /**
     * Getter for property 'elasticClient'.
     *
     * @return Value for property 'elasticClient'.
     */
    public MultiElasticClientIF getElasticClient() {
        return elasticClient;
    }

    /**
     * Setter for property 'elasticClient'.
     *
     * @param elasticClient Value to set for property 'elasticClient'.
     */
    public void setElasticClient(MultiElasticClientIF elasticClient) {
        this.elasticClient = elasticClient;
    }

    /**
     * Setter for property 'sentenceScoringEnabled'.
     *
     * @param sentenceScoringEnabled Value to set for property 'sentenceScoringEnabled'.
     */
    public void setSentenceScoringEnabled(boolean sentenceScoringEnabled) {
        this.sentenceScoringEnabled = sentenceScoringEnabled;
    }

    /**
     * Setter for property 'minTokenLenght'.
     *
     * @param minTokenLenght Value to set for property 'minTokenLenght'.
     */
    public void setMinTokenLenght(int minTokenLenght) {
        this.minTokenLenght = minTokenLenght;
    }

    /**
     * Setter for property 'maxTokenLenght'.
     *
     * @param maxTokenLenght Value to set for property 'maxTokenLenght'.
     */
    public void setMaxTokenLenght(int maxTokenLenght) {
        this.maxTokenLenght = maxTokenLenght;
    }

    public int getMinTokenWeight() {
        return minTokenWeight;
    }

    public void setMinTokenWeight(int minTokenWeight) {
        this.minTokenWeight = minTokenWeight;
    }
}
