package com.quasiris.qsf.pipeline.filter.elastic.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
public class InnerHits {

    private Object total;

    private Double max_score;

    private List<InnerHit> hits;

    public void setTotal(Object total) {
        this.total = total;
    }

    public Long getTotal() {
        if(this.total == null) {
            return null;
        }
        if(this.total instanceof Number) {
            return ((Number) this.total).longValue();
        } else if(this.total instanceof Map) {
            return ((Number) ((Map) this.total).get("value")).longValue();
        }
        return null;
    }

    public Double getMax_score() {
        return max_score;
    }

    public void setMax_score(Double max_score) {
        this.max_score = max_score;
    }

    public List<InnerHit> getHits() {
        return hits;
    }

    public void setHits(List<InnerHit> hits) {
        this.hits = hits;
    }

    @Override
    public String toString() {
        return "InnerHits{" +
                "total=" + total +
                ", max_score=" + max_score +
                ", hits=" + hits +
                '}';
    }
}
