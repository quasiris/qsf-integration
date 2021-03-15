package com.quasiris.qsf.pipeline.filter.elastic.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InnerHitResult {
    private Hits hits;

    public Hits getHits() {
        return hits;
    }

    public void setHits(Hits hits) {
        this.hits = hits;
    }

    @Override
    public String toString() {
        return "Hits{" +
                ", hits=" + hits +
                '}';
    }
}
