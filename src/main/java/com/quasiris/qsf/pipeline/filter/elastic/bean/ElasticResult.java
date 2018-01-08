package com.quasiris.qsf.pipeline.filter.elastic.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by mki on 19.11.17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticResult {

    private long took;

    private Hits hits;

    public long getTook() {
        return took;
    }

    public void setTook(long took) {
        this.took = took;
    }

    public Hits getHits() {
        return hits;
    }

    public void setHits(Hits hits) {
        this.hits = hits;
    }


    @Override
    public String toString() {
        return "ElasticResult{" +
                "took=" + took +
                ", hits=" + hits +
                '}';
    }
}
