package com.quasiris.qsf.query;

/**
 * Created by mki on 11.11.16.
 */
public enum FilterType {

    TERM("term"),RANGE("range"),SLIDER("slider");


    private final String code;

    FilterType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}