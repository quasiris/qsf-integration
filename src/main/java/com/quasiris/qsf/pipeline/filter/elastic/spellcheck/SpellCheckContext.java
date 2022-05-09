package com.quasiris.qsf.pipeline.filter.elastic.spellcheck;

import com.quasiris.qsf.dto.response.DidYouMeanResult;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.elastic.SpellCheckToken;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SpellCheckContext {

    public static final String SPELLCHECK_CONTEXT = "spellcheckContext";


    private List<SpellCheckToken> spellCheckTokens;

    private DidYouMeanResult didYouMeanResult;

    private Set<String> tags;

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

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
    public void addTag(String tag) {
        if(this.tags == null) {
            this.tags = new LinkedHashSet<>();
        }
        this.tags.add(tag);
    }
}
