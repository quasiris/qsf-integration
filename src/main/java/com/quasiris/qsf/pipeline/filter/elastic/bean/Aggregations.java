package com.quasiris.qsf.pipeline.filter.elastic.bean;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.quasiris.qsf.pipeline.filter.elastic.AggregationsDeserializer;

import java.util.List;

@JsonDeserialize(using = AggregationsDeserializer.class)
public class Aggregations {
    private Long doc_count;
    private List<Aggregation> aggregations;

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
    public List<Aggregation> getAggregations() {
        return aggregations;
    }

    /**
     * Setter for property 'aggregations'.
     *
     * @param aggregations Value to set for property 'aggregations'.
     */
    public void setAggregations(List<Aggregation> aggregations) {
        this.aggregations = aggregations;
    }
}
