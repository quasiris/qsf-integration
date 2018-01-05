package de.quasiris.qsf.pipeline.filter.elastic.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by mki on 19.11.17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Hits {

    private Long total;

    private Double max_score;

    private List<Hit> hits;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Double getMax_score() {
        return max_score;
    }

    public void setMax_score(Double max_score) {
        this.max_score = max_score;
    }

    public List<Hit> getHits() {
        return hits;
    }

    public void setHits(List<Hit> hits) {
        this.hits = hits;
    }

    @Override
    public String toString() {
        return "Hits{" +
                "total=" + total +
                ", max_score=" + max_score +
                ", hits=" + hits +
                '}';
    }
}
