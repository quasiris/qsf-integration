package com.quasiris.qsf.pipeline.filter.elastic.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.quasiris.qsf.pipeline.filter.elastic.AggregationsDeserializer;

/**
 * Created by mki on 19.11.17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticResult {

    private long took;

    private Hits hits;

    @JsonDeserialize(using = AggregationsDeserializer.class)
    private Aggregation aggregations;

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

    /**
     * Getter for property 'aggregations'.
     *
     * @return Value for property 'aggregations'.
     */
    public Aggregation getAggregations() {
        return aggregations;
    }

    /**
     * Setter for property 'aggregations'.
     *
     * @param aggregations Value to set for property 'aggregations'.
     */
    public void setAggregations(Aggregation aggregations) {
        this.aggregations = aggregations;
    }

    @Override
    public String toString() {
        return "ElasticResult{" +
                "took=" + took +
                ", hits=" + hits +
                ", aggregations=" + aggregations +
                '}';
    }
}
