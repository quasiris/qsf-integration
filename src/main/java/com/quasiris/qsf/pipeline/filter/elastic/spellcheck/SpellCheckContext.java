package com.quasiris.qsf.pipeline.filter.elastic.spellcheck;

import com.quasiris.qsf.dto.response.DidYouMeanResult;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.elastic.SpellCheckToken;

import java.util.List;

public class SpellCheckContext {

    public static final String SPELLCHECK_CONTEXT = "spellcheckContext";


    private List<SpellCheckToken> spellCheckTokens;

    private DidYouMeanResult didYouMeanResult;

    private String tags;

    public static SpellCheckContext get(PipelineContainer pipelineContainer) {
        SpellCheckContext spellCheckContext = pipelineContainer.getContext(SpellCheckContext.SPELLCHECK_CONTEXT, SpellCheckContext.class);
        if(spellCheckContext == null) {
            spellCheckContext = new SpellCheckContext();
            pipelineContainer.putContext(SPELLCHECK_CONTEXT, spellCheckContext);
        }
        return spellCheckContext;
    }

    public List<SpellCheckToken> getSpellCheckTokens() {
        return spellCheckTokens;
    }

    public void setSpellCheckTokens(List<SpellCheckToken> spellCheckTokens) {
        this.spellCheckTokens = spellCheckTokens;
    }

    public DidYouMeanResult getDidYouMeanResult() {
        return didYouMeanResult;
    }

    public void setDidYouMeanResult(DidYouMeanResult didYouMeanResult) {
        this.didYouMeanResult = didYouMeanResult;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
