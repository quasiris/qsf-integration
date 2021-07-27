package com.quasiris.qsf.test.service;

import java.util.HashMap;
import java.util.Map;

public enum Operator {


    EQUALS("equals"),
    EQUALS_IGNORE_CASE("equalsIgnoreCase"),
    CONTAINS("contains"),
    CONTAINS_IGNORE_CASE("containsIgnoreCase"),
    STARTS_WITH("startsWith"),
    STARTS_WITH_IGNORE_CASE("startsWithIgnoreCase"),
    IS_DATE_TIME("isDateTime"),
    IS_PATH("isPath"),
    IS_URL("isUrl"),
    IS_BOOLEAN("isBoolean"),
    IS_NUMBER("isNumber"),
    IS_STRING("isString"),
    EXISISTS("exists"),
    GREATER("greeater"),
    GREATER_EQUALS("greeaterequals"),
    LESS("less"),
    LESS_EQUALS("lessequals");


    private final String code;

    private static Map<String, Operator> operatorMapping = new HashMap<>();
    static {
        operatorMapping.put("equals", EQUALS);
        // deprecated
        operatorMapping.put("equalsLowerCase", EQUALS_IGNORE_CASE);
        operatorMapping.put("equalsIgnoreCase", EQUALS_IGNORE_CASE);
        operatorMapping.put("contains", CONTAINS);
        // deprecated
        operatorMapping.put("containsLowerCase", CONTAINS_IGNORE_CASE);
        operatorMapping.put("containsIgnoreCase", CONTAINS_IGNORE_CASE);
        operatorMapping.put("startsWith", STARTS_WITH);
        // deprecated
        operatorMapping.put("startsWithLowerCase", STARTS_WITH_IGNORE_CASE);
        operatorMapping.put("startsWithIgnoreCase", STARTS_WITH_IGNORE_CASE);
        operatorMapping.put("isDateTime", IS_DATE_TIME);
        operatorMapping.put("isPath", IS_PATH);
        operatorMapping.put("isUrl", IS_URL);
        operatorMapping.put("isBoolean", IS_BOOLEAN);
        operatorMapping.put("isNumber", IS_NUMBER);
        operatorMapping.put("isString", IS_STRING);
        operatorMapping.put("exists", EXISISTS);
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
