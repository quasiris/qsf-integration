package com.quasiris.qsf.query;

/**
 * Created by mki on 4.3.18.
 */
public enum UpperLowerBound {

    LOWER_INCLUDED("[", "gte"),
    LOWER_EXCLUDED("{", "gt"),
    UPPER_INCLUDED("]", "lte"),
    UPPER_EXCLUDED("}", "lt");


    private final String code;

    private final String operator;

    UpperLowerBound(String code, String operator) {
        this.code = code;
        this.operator = operator;
    }

    public String getCode() {
        return code;
    }

    public String getOperator() {
        return operator;
    }

    public boolean isLowerIncluded(){
        return LOWER_INCLUDED.equals(this);
    }

    public boolean isLowerExcluded(){
        return LOWER_EXCLUDED.equals(this);
    }

    public boolean isUpperIncluded(){
        return UPPER_INCLUDED.equals(this);
    }

    public boolean isUpperExcluded(){
        return UPPER_EXCLUDED.equals(this);
    }
}
