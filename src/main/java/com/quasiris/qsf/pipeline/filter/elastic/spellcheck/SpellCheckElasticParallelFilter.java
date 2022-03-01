package com.quasiris.qsf.pipeline.filter.elastic.spellcheck;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.pipeline.exception.PipelineRestartException;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.pipeline.filter.elastic.SpellCheckToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by tbl on 27.2.22.
 */
public class SpellCheckElasticParallelFilter extends AbstractFilter {

    private static Logger LOG = LoggerFactory.getLogger(SpellCheckElasticParallelFilter.class);

    private int minTokenLenght = 4;
    private int maxTokenLenght = 10;
    private int minTokenWeight = 1;

    private String baseUrl;

    private SpellCheckElasticClient spellCheckElasticClient;


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
        SpellCheckContext spellCheckContext = SpellCheckContext.get(pipelineContainer);
        spellCheckContext.setSpellCheckTokens(spellCheckTokens);
    }




    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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

    public void setSpellCheckElasticClient(SpellCheckElasticClient spellCheckElasticClient) {
        this.spellCheckElasticClient = spellCheckElasticClient;
    }
}
