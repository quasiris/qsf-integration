package com.quasiris.qsf.pipeline.filter.elastic.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by tbl on 16.04.20.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Analyze {

    private List<AnalyzeToken> tokens;

    /**
     * Getter for property 'tokens'.
     *
     * @return Value for property 'tokens'.
     */
    public List<AnalyzeToken> getTokens() {
        return tokens;
    }

    /**
     * Setter for property 'tokens'.
     *
     * @param tokens Value to set for property 'tokens'.
     */
    public void setTokens(List<AnalyzeToken> tokens) {
        this.tokens = tokens;
    }
}
