package com.quasiris.qsf.pipeline.filter.elastic.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by mki on 04.02.18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bucket {

    private String key;
    private Long doc_count;


    private Aggregation subFacet;

    private VariantCount variant_count;

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

    /**
     * Getter for property 'variant_count'.
     *
     * @return Value for property 'variant_count'.
     */
    public VariantCount getVariant_count() {
        return variant_count;
    }

    /**
     * Setter for property 'variant_count'.
     *
     * @param variant_count Value to set for property 'variant_count'.
     */
    public void setVariant_count(VariantCount variant_count) {
        this.variant_count = variant_count;
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
