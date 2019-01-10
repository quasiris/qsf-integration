package com.quasiris.qsf.pipeline.filter.elastic.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by tbl on 22.12.18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MultiElasticResult {


    List<ElasticResult> responses;

    public List<ElasticResult> getResponses() {
        return responses;
    }

    public void setResponses(List<ElasticResult> responses) {
        this.responses = responses;
    }

    @Override
    public String toString() {
        return "MultiElasticResult{" +
                "responses=" + responses +
                '}';
    }
}
