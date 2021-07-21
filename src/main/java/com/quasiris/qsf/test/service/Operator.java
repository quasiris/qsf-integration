package com.quasiris.qsf.test.service;

import java.util.HashMap;
import java.util.Map;

public enum Operator {


    EQUALS("equals"),
    GREATER("greeater"),
    GREATER_EQUALS("greeaterequals"),
    LESS("less"),
    LESS_EQUALS("lessequals");


    private final String code;

    private static Map<String, Operator> operatorMapping = new HashMap<>();
    static {
        operatorMapping.put("equals", EQUALS);
        operatorMapping.put("=", EQUALS);
        operatorMapping.put("greater", GREATER);
        operatorMapping.put(">", GREATER);
        operatorMapping.put("less", LESS);
        operatorMapping.put("<", LESS);
        operatorMapping.put("greaterequals", GREATER_EQUALS);
        operatorMapping.put(">=", GREATER_EQUALS);
        operatorMapping.put("lessequals", LESS_EQUALS);
        operatorMapping.put("<=", LESS_EQUALS);
    }

    Operator(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Operator getOperator(String operator) {
        Operator o =  operatorMapping.get(operator);
        if(o == null) {
            throw new IllegalArgumentException("The operator " + operator + " is unknown.");
        }
        return o;
    }

    public boolean isEquals() {
        return this == Operator.EQUALS;
    }

    public boolean isGreater() {
        return this == Operator.GREATER;
    }

    public boolean isLess() {
        return this == Operator.LESS;
    }

    public boolean isLessEquals() {
        return this == Operator.LESS_EQUALS;
    }

    public boolean isGreaterEquals() {
        return this == Operator.GREATER_EQUALS;
    }

}
