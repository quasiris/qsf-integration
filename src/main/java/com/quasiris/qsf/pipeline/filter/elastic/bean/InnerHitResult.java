package com.quasiris.qsf.pipeline.filter.elastic.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InnerHitResult {
    private InnerHits hits;

    public InnerHits getHits() {
        return hits;
    }

    public void setHits(InnerHits hits) {
        this.hits = hits;
    }

    @Override
    public String toString() {
        return "InnerHitResult{" +
                ", hits=" + hits +
                '}';
    }
}
