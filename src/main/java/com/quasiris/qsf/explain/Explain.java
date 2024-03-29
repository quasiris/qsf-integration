package com.quasiris.qsf.explain;

import java.util.List;

public class Explain<T> {

    private String id;
    private String name;
    private ExplainDataType dataType = ExplainDataType.JSON;
    private String type = "u";
    private T explainObject;
    private String treadId;

    public Explain() {
        this.treadId = Thread.currentThread().getName();
    }

    private List<Explain> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExplainDataType getDataType() {
        return dataType;
    }

    public void setDataType(ExplainDataType dataType) {
        this.dataType = dataType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getExplainObject() {
        return explainObject;
    }

    public void setExplainObject(T explainObject) {
        this.explainObject = explainObject;
    }

    public List<Explain> getChildren() {
        return children;
    }

    public void setChildren(List<Explain> children) {
        this.children = children;
    }

    public String getTreadId() {
        return treadId;
    }

    public void setTreadId(String treadId) {
        this.treadId = treadId;
    }

    @Override
    public String toString() {
        return type + " -> " + id;

    }
}
