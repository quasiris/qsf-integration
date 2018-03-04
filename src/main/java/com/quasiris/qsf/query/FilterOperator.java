package com.quasiris.qsf.query;

/**
 * Created by mki on 11.11.16.
 */
public enum FilterOperator {

    OR("or"),AND("and");


    private final String code;

    FilterOperator(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
