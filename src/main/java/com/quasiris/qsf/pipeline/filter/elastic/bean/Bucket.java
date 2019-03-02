package com.quasiris.qsf.pipeline.filter.elastic.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by mki on 04.02.18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bucket {

   private String key;
   private Long doc_count;

    Aggregation subFacet;


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getDoc_count() {
        return doc_count;
    }

    public void setDoc_count(Long doc_count) {
        this.doc_count = doc_count;
    }

    public Aggregation getSubFacet() {
        return subFacet;
    }

    public void setSubFacet(Aggregation subFacet) {
        this.subFacet = subFacet;
    }

    @Override
    public String toString() {
        return "Bucket{" +
                "key='" + key + '\'' +
                ", doc_count=" + doc_count +
                ", subFacet=" + subFacet +
                '}';
    }
}
