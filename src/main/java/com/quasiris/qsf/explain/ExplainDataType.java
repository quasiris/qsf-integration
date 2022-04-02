package com.quasiris.qsf.explain;

public enum ExplainDataType {

    JSON,
    OBJECT,
    STRING;


    public boolean isJson() {
        return this == JSON;
    }

    public boolean isObject() {
        return this == OBJECT;
    }
}
