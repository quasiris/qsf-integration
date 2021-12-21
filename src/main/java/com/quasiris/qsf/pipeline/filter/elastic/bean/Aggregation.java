package com.quasiris.qsf.pipeline.filter.elastic.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by mki on 04.02.18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Aggregation {

    private String key;
    private Integer doc_count_error_upper_bound;

    private Integer sum_other_doc_count;

    private Long value;
    List<Bucket> buckets;

    private Integer count;
    private Double min;
    private Double max;
    private Double avg;
    private Double sum;

    private Long doc_count;
    private List<Aggregation> aggregations;

    /**
     * Getter for property 'doc_count'.
     *
     * @return Value for property 'doc_count'.
     */
    public Long getDoc_count() {
        return doc_count;
    }

    /**
     * Setter for property 'doc_count'.
     *
     * @param doc_count Value to set for property 'doc_count'.
     */
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

    /**
     * Getter for property 'key'.
     *
     * @return Value for property 'key'.
     */
    public String getKey() {
        return key;
    }

    /**
     * Setter for property 'key'.
     *
     * @param key Value to set for property 'key'.
     */
    public void setKey(String key) {
        this.key = key;
    }

    public Integer getDoc_count_error_upper_bound() {
        return doc_count_error_upper_bound;
    }

    public void setDoc_count_error_upper_bound(Integer doc_count_error_upper_bound) {
        this.doc_count_error_upper_bound = doc_count_error_upper_bound;
    }

    public Integer getSum_other_doc_count() {
        return sum_other_doc_count;
    }

    public void setSum_other_doc_count(Integer sum_other_doc_count) {
        this.sum_other_doc_count = sum_other_doc_count;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public List<Bucket> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<Bucket> buckets) {
        this.buckets = buckets;
    }


    /**
     * Getter for property 'count'.
     *
     * @return Value for property 'count'.
     */
    public Integer getCount() {
        return count;
    }

    /**
     * Setter for property 'count'.
     *
     * @param count Value to set for property 'count'.
     */
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * Getter for property 'min'.
     *
     * @return Value for property 'min'.
     */
    public Double getMin() {
        return min;
    }

    /**
     * Setter for property 'min'.
     *
     * @param min Value to set for property 'min'.
     */
    public void setMin(Double min) {
        this.min = min;
    }

    /**
     * Getter for property 'max'.
     *
     * @return Value for property 'max'.
     */
    public Double getMax() {
        return max;
    }

    /**
     * Setter for property 'max'.
     *
     * @param max Value to set for property 'max'.
     */
    public void setMax(Double max) {
        this.max = max;
    }

    /**
     * Getter for property 'avg'.
     *
     * @return Value for property 'avg'.
     */
    public Double getAvg() {
        return avg;
    }

    /**
     * Setter for property 'avg'.
     *
     * @param avg Value to set for property 'avg'.
     */
    public void setAvg(Double avg) {
        this.avg = avg;
    }

    /**
     * Getter for property 'sum'.
     *
     * @return Value for property 'sum'.
     */
    public Double getSum() {
        return sum;
    }

    /**
     * Setter for property 'sum'.
     *
     * @param sum Value to set for property 'sum'.
     */
    public void setSum(Double sum) {
        this.sum = sum;
    }

    @Override
    public String toString() {
        return "Aggregation{" +
                "doc_count_error_upper_bound=" + doc_count_error_upper_bound +
                ", sum_other_doc_count=" + sum_other_doc_count +
                ", buckets=" + buckets +
                ", count=" + count +
                ", min=" + min +
                ", max=" + max +
                ", avg=" + avg +
                ", sum=" + sum +
                '}';
    }
}
