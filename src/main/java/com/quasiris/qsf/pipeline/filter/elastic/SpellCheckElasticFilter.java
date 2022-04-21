package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.dto.response.DidYouMeanResult;
import com.quasiris.qsf.exception.DebugType;
import com.quasiris.qsf.explain.ExplainContextHolder;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.pipeline.exception.PipelineRestartException;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.pipeline.filter.elastic.client.QSFHttpClient;
import com.quasiris.qsf.pipeline.filter.elastic.spellcheck.SpellCheckContext;
import com.quasiris.qsf.pipeline.filter.elastic.spellcheck.SpellCheckElasticClient;
import com.quasiris.qsf.pipeline.filter.elastic.spellcheck.SpellcheckUtils;
import com.quasiris.qsf.pipeline.filter.elastic.spellcheck.SpellcheckVariants;
import com.quasiris.qsf.query.SearchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    private SpellCheckElasticClient spellCheckElasticClient;

    private boolean sentenceScoringEnabled = true;

    private String restartPipelineId;

    public SpellCheckElasticFilter() {
    }

    @Override
    public void init() {
        if(spellCheckElasticClient == null) {
            spellCheckElasticClient = new SpellCheckElasticClient(baseUrl, minTokenLenght, minTokenWeight);
        }
        super.init();

    }

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        process(pipelineContainer);
        return pipelineContainer;
    }


    private void process(PipelineContainer pipelineContainer) throws IOException, PipelineContainerException, PipelineRestartException {

        if(pipelineContainer.getSearchQuery().getQueryToken().size() > maxTokenLenght) {
            return;
        }

        List<SpellCheckToken> spellCheckTokens = spellCheckElasticClient.spellspeck(pipelineContainer.getSearchQuery());
        ExplainContextHolder.getContext().explainJson(getId() + ".spellCheckTokens", spellCheckTokens);

        if(pipelineContainer.isDebugEnabled()) {
            pipelineContainer.debug("spellCheckTokens", DebugType.JSON, spellCheckTokens);
        }

        SpellcheckVariants spellcheckVariants = new SpellcheckVariants();
        List<Score> correctedQueryVariants = spellcheckVariants.computeVariants(spellCheckTokens);


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
        if(!SpellcheckUtils.fuzzyEquals(corrected.getText(), searchQuery.getQ())) {
            searchQuery.setOriginalQuery(searchQuery.getQ());
            searchQuery.setQ(corrected.getText());
            searchQuery.setQueryChanged(true);
            searchQuery.addQueryChangedReason("spellcheck");

            DidYouMeanResult didYouMeanResult = new DidYouMeanResult();
            didYouMeanResult.setName("spellCorrection");
            didYouMeanResult.setType("corrected");
            didYouMeanResult.setCorrected(corrected.getText());
            didYouMeanResult.setOriginal(searchQuery.getOriginalQuery());
            SpellCheckContext.get(pipelineContainer).setDidYouMeanResult(didYouMeanResult);
            if(restartPipelineId != null) {
                throw new PipelineRestartException(restartPipelineId);
            }
        }
    }




    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Setter for property 'sentenceScoringEnabled'.
     *
     * @param sentenceScoringEnabled Value to set for property 'sentenceScoringEnabled'.
     */
    public void setSentenceScoringEnabled(boolean sentenceScoringEnabled) {
        this.sentenceScoringEnabled = sentenceScoringEnabled;
    }

    public boolean isSentenceScoringEnabled() {
        return sentenceScoringEnabled;
    }

    /**
     * Setter for property 'minTokenLenght'.
     *
     * @param minTokenLenght Value to set for property 'minTokenLenght'.
     */
    public void setMinTokenLenght(int minTokenLenght) {
        this.minTokenLenght = minTokenLenght;
    }

    public int getMinTokenLenght() {
        return minTokenLenght;
    }

    /**
     * Setter for property 'maxTokenLenght'.
     *
     * @param maxTokenLenght Value to set for property 'maxTokenLenght'.
     */
    public void setMaxTokenLenght(int maxTokenLenght) {
        this.maxTokenLenght = maxTokenLenght;
    }

    public int getMaxTokenLenght() {
        return maxTokenLenght;
    }

    public int getMinTokenWeight() {
        return minTokenWeight;
    }

    public void setMinTokenWeight(int minTokenWeight) {
        this.minTokenWeight = minTokenWeight;
    }

    /**
     * Setter for property 'restartPipelineId'.
     *
     * @param restartPipelineId Value to set for property 'restartPipelineId'.
     */
    public void setRestartPipelineId(String restartPipelineId) {
        this.restartPipelineId = restartPipelineId;
    }

    public String getRestartPipelineId() {
        return restartPipelineId;
    }

    public void setSpellCheckElasticClient(SpellCheckElasticClient spellCheckElasticClient) {
        this.spellCheckElasticClient = spellCheckElasticClient;
    }
}
