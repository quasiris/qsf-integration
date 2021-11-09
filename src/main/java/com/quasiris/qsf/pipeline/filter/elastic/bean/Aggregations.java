package com.quasiris.qsf.pipeline.filter.elastic.bean;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.quasiris.qsf.pipeline.filter.elastic.AggregationsDeserializer;

import java.util.HashMap;
import java.util.Map;

@JsonDeserialize(using = AggregationsDeserializer.class)
public class Aggregations {
    private Long doc_count;
    private Map<String, Aggregation> aggregations;

    public void put(String key, Aggregation aggregation) {
        if(this.aggregations == null) {
            this.aggregations = new HashMap<>();
        }
        this.aggregations.put(key,aggregation);
    }

    public Long getDoc_count() {
        return doc_count;
    }

    public void setDoc_count(Long doc_count) {
        this.doc_count = doc_count;
    }

    /**
     * Getter for property 'aggregations'.
     *
     * @return Value for property 'aggregations'.
     */
    public Map<String, Aggregation> getAggregations() {
        return aggregations;
    }

    /**
     * Setter for property 'aggregations'.
     *
     * @param aggregations Value to set for property 'aggregations'.
     */
    public void setAggregations(Map<String, Aggregation> aggregations) {
        this.aggregations = aggregations;
    }
}
