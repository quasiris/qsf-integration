package com.quasiris.qsf.exception;

public enum DebugType {

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
