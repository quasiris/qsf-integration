package com.quasiris.qsf.pipeline.filter.elastic.bean;

import java.util.List;

/**
 * Created by mki on 04.02.18.
 */
public class Aggregation {

    private Integer doc_count_error_upper_bound;

    private Integer sum_other_doc_count;

    List<Bucket> buckets;

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

    public List<Bucket> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<Bucket> buckets) {
        this.buckets = buckets;
    }

    @Override
    public String toString() {
        return "Aggregation{" +
                "doc_count_error_upper_bound=" + doc_count_error_upper_bound +
                ", sum_other_doc_count=" + sum_other_doc_count +
                ", buckets=" + buckets +
                '}';
    }
}
