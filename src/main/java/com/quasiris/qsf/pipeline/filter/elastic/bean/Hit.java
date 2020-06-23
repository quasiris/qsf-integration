package com.quasiris.qsf.pipeline.filter.elastic.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mki on 19.11.17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Hit {

    private String _index;
    private String _type;
    private String _id;
    private Double _score;
    private ObjectNode _source;
    private ObjectNode _explanation;
    private LinkedHashMap<String, InnerHitResult> inner_hits;

    private Map<String, List<String>> highlight;


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

    public ObjectNode get_source() {
        return _source;
    }

    public void set_source(ObjectNode _source) {
        this._source = _source;
    }

    public ObjectNode get_explanation() {
        return _explanation;
    }

    public void set_explanation(ObjectNode _explanation) {
        this._explanation = _explanation;
    }

    public LinkedHashMap<String, InnerHitResult> getInner_hits() {
        return inner_hits;
    }

    public void getInner_hits(LinkedHashMap<String, InnerHitResult> inner_hits) {
        this.inner_hits = inner_hits;
    }

    public Map<String, List<String>> getHighlight() {
        return highlight;
    }

    public void setHighlight(Map<String, List<String>> highlight) {
        this.highlight = highlight;
    }

    @Override
    public String toString() {
        return "Hit{" +
                "_index='" + _index + '\'' +
                ", _type='" + _type + '\'' +
                ", _id='" + _id + '\'' +
                ", _score=" + _score +
                ", _source=" + _source +
                ", highlight=" + highlight +
                '}';
    }
}
