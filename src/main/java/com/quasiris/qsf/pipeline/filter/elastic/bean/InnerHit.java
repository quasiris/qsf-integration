package com.quasiris.qsf.pipeline.filter.elastic.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InnerHit {

    private String _index;
    private String _type;
    private String _id;
    private Double _score;
    private InnerHitNested _nested;

    public String get_index() {
        return _index;
    }

    public void set_index(String _index) {
        this._index = _index;
    }

    public String get_type() {
        return _type;
    }

    public void set_type(String _type) {
        this._type = _type;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public Double get_score() {
        return _score;
    }

    public void set_score(Double _score) {
        this._score = _score;
    }


    public InnerHitNested get_nested() {
        return _nested;
    }

    public void set_nested(InnerHitNested _nested) {
        this._nested = _nested;
    }

    @Override
    public String toString() {
        return "Hit{" +
                "_index='" + _index +
                ", _type='" + _type +
                ", _id='" + _id +
                ", _score=" + _score +
                ", _nested=" + _nested +
                '}';
    }
}
