package com.quasiris.qsf.test.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum to determine between operators that can be used to compare two values<br>
 * The following codes are available:
 * <ul>
 *     <li>"equals", "=" for operator EQUALS</li>
 *     <li>"equalsIgnoreCase" for operator EQUALS_IGNORE_CASE</li>
 *     <li>"contains" for operator CONTAINS</li>
 *     <li>"containsIgnoreCase" for operator CONTAINS_IGNORE_CASE</li>
 *     <li>"startsWith" for operator STARTS_WITH</li>
 *     <li>"startsWithIgnoreCase" for operator STARTS_WITH_IGNORE_CASE</li>
 *     <li>"isDateTime" for operator IS_DATE_TIME</li>
 *     <li>"isUri" for operator IS_URI</li>
 *     <li>"isUrl" for operator IS_URL</li>
 *     <li>"isBoolean" for operator IS_BOOLEAN</li>
 *     <li>"isNumber" for operator IS_NUMBER</li>
 *     <li>"isString" for operator IS_STRING</li>
 *     <li>"exists" for operator EXISTS</li>
 *     <li>"greater", ">" for operator GREATER</li>
 *     <li>"greaterEquals", ">=" for operator GREATER_EQUALS</li>
 *     <li>"less", "<" for operator LESS</li>
 *     <li>"lessEquals", "<=" for operator LESS_EQUALS</li>
 * </ul>
 */
public enum Operator {

    EQUALS("equals"),
    EQUALS_IGNORE_CASE("equalsIgnoreCase"),
    CONTAINS("contains"),
    CONTAINS_IGNORE_CASE("containsIgnoreCase"),
    STARTS_WITH("startsWith"),
    STARTS_WITH_IGNORE_CASE("startsWithIgnoreCase"),
    IS_DATE_TIME("isDateTime"),
    IS_URI("isUri"),
    IS_URL("isUrl"),
    IS_BOOLEAN("isBoolean"),
    IS_NUMBER("isNumber"),
    IS_STRING("isString"),
    EXISTS("exists"),
    GREATER("greater"),
    GREATER_EQUALS("greaterEquals"),
    LESS("less"),
    LESS_EQUALS("lessEquals");


    /**
     * the string code that is used to determine its associated operator<br>
     * see {@link Operator} for available operator codes
     */
    private final String code;

    /**
     * operatorMapping maps a string code to its associated operator<br>
     * see {@link Operator} for which operator code map to which operator
     */
    private static final Map<String, Operator> operatorMapping = new HashMap<>();
    static {
        operatorMapping.put("equals", EQUALS);
        // "equalsLowerCase" is deprecated since it is equivalent to equalsIgnoreCase
        operatorMapping.put("equalsLowerCase", EQUALS_IGNORE_CASE);
        operatorMapping.put("equalsIgnoreCase", EQUALS_IGNORE_CASE);
        operatorMapping.put("contains", CONTAINS);
        // "containsLowerCase" is deprecated since it is equivalent to containsIgnoreCase
        operatorMapping.put("containsLowerCase", CONTAINS_IGNORE_CASE);
        operatorMapping.put("containsIgnoreCase", CONTAINS_IGNORE_CASE);
        operatorMapping.put("startsWith", STARTS_WITH);
        // "startsWithLowerCase" is deprecated
        operatorMapping.put("startsWithLowerCase", STARTS_WITH_IGNORE_CASE);
        operatorMapping.put("startsWithIgnoreCase", STARTS_WITH_IGNORE_CASE);
        operatorMapping.put("isDateTime", IS_DATE_TIME);
        operatorMapping.put("isUri", IS_URI);
        operatorMapping.put("isUrl", IS_URL);
        operatorMapping.put("isBoolean", IS_BOOLEAN);
        operatorMapping.put("isNumber", IS_NUMBER);
        operatorMapping.put("isString", IS_STRING);
        operatorMapping.put("exists", EXISTS);
        operatorMapping.put("=", EQUALS);
        operatorMapping.put("greater", GREATER);
        operatorMapping.put(">", GREATER);
        // "greaterequals" is deprecated since camel case should be used
        operatorMapping.put("greaterequals", GREATER_EQUALS);
        operatorMapping.put("greaterEquals", GREATER_EQUALS);
        operatorMapping.put(">=", GREATER_EQUALS);
        operatorMapping.put("less", LESS);
        operatorMapping.put("<", LESS);
        // "lessequals" is deprecated since camel case should be used
        operatorMapping.put("lessequals", LESS_EQUALS);
        operatorMapping.put("lessEquals", LESS_EQUALS);
        operatorMapping.put("<=", LESS_EQUALS);
    }

    /**
     * constructs an operator from a String code that is associated with it
     * @param code String, code that is associated with the operator<br>
     *             see {@link Operator} for available operator codes
     */
    Operator(String code) {
        this.code = code;
    }

    /**
     * returns the String code that is associated with this operator object
     * @return String, code that is associated with the operator<br>
     *         see {@link Operator} for available operator codes
     */
    public String getCode() {
        return code;
    }

    /**
     * returns an operator Object by specifying a code that is associated with it
     * @param code String, the operator code for which the associated operator should be returned <br>
     *             see {@link Operator} for available operator codes
     * @return {@link Operator}, the operator Object that is associated with the code
     * @throws IllegalArgumentException if the operator code is unknown
     */
    public static Operator getOperator(String code) {
        Operator op =  operatorMapping.get(code);
        if(op == null) {
            throw new IllegalArgumentException("The operator " + code + " is unknown.");
        }
        return op;
    }


    // All the following methods are defined for when using Operator as enum and checking for a specific type
    // For some operators these were already implemented, so I implemented the rest to ensure compatibility

    public boolean isEquals() {
        return this == Operator.EQUALS;
    }

    public boolean isEqualsIgnoreCase() {
        return this == Operator.EQUALS_IGNORE_CASE;
    }

    public boolean isContains() {
        return this == Operator.CONTAINS;
    }

    public boolean isContainsIgnoreCase() {
        return this == Operator.CONTAINS_IGNORE_CASE;
    }

    public boolean isStartsWith() {
        return this == Operator.STARTS_WITH;
    }

    public boolean isStartsWithIgnoreCase() {
        return this == Operator.STARTS_WITH_IGNORE_CASE;
    }

    public boolean isIsDateTime() {
        return this == Operator.IS_DATE_TIME;
    }

    public boolean isIsUri() {
        return this == Operator.IS_URI;
    }

    public boolean isIsUrl() {
        return this == Operator.IS_URL;
    }

    public boolean isIsBoolean() {
        return this == Operator.IS_BOOLEAN;
    }

    public boolean isIsNumber() {
        return this == Operator.IS_NUMBER;
    }

    public boolean isIsString() {
        return this == Operator.IS_STRING;
    }

    public boolean isExists() {
        return this == Operator.EXISTS;
    }

    public boolean isGreater() {
        return this == Operator.GREATER;
    }

    public boolean isGreaterEquals() {
        return this == Operator.GREATER_EQUALS;
    }

    public boolean isLess() {
        return this == Operator.LESS;
    }

    public boolean isLessEquals() {
        return this == Operator.LESS_EQUALS;
    }

}
