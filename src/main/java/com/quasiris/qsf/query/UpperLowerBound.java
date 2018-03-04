package com.quasiris.qsf.query;

/**
 * Created by mki on 4.3.18.
 */
public enum UpperLowerBound {

    LOWER_INCLUDED("["),LOWER_EXCLUDED("{"),UPPER_INCLUDED("]"),UPPER_EXCLUDED("}");


    private final String code;

    UpperLowerBound(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
