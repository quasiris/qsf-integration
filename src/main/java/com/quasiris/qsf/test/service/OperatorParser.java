package com.quasiris.qsf.test.service;

import com.quasiris.qsf.commons.util.DateUtil;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Operator Parser is used to parse an {@link OperatorParser#operator} and a {@link OperatorParser#parsedValue} from a rawValue <br><br>
 * The {@link OperatorParser#eval(Object)} method can be used to compare an actual value to the {@link OperatorParser#parsedValue} using the {@link OperatorParser#operator}
 */
public class OperatorParser {

    private Object rawValue;
    private Object parsedValue;

    private Operator operator;

    private boolean parsed = false;

    /**
     * constructs an OperatorParser from a raw rawValue and parses {@link OperatorParser#operator} and {@link OperatorParser#parsedValue}
     * @param rawValue Object, the value for which should be tested <br>
     *                 The value can be a String of form "[operator]:[rawValue]" or just the rawValue <br>
     *                 see {@link Operator} for available operator codes
     */
    public OperatorParser(Object rawValue) {
        this.rawValue = rawValue;
        parseRawValue();
    }

    /**
     * Parses the rawValue into a parsedValue, sets {@link OperatorParser#operator} and {@link OperatorParser#operator} and sets parsed to true<br>
     *      If the rawValue is a String, it gets parsed by {@link OperatorParser#parseString(String)} <br>
     *      If the rawValue is not a String, the parsedValue is the rawValue (no parsing needed) and operator is {@link Operator#EQUALS}
     */
    public void parseRawValue() {

        // if rawValue is a String parse it, if not use EQUALS operator
        if(rawValue instanceof String) {
            parseString((String) rawValue);
        } else {
            this.operator = Operator.EQUALS;
            this.parsedValue = rawValue;
        }
        parsed = true;
    }

    /**
     * parses a rawValue String, sets the parsedValue, operator and sets parsed to true
     * @param stringValue String, A rawValue String of form "[operator]:[rawValue]" or just the rawValue
     *                    <ul><li>1: If no operator is specified, {@link Operator#EQUALS} will be used and parsedValue is stringValue</li>
     *                    <li>2: If operator was used but is unknown {@link Operator#EQUALS} will be used and parsedValue is stringValue</li>
     *                    <li>3: If operator was used and is known, rawValue will be parsed into parsedValue</li></ul>
     *                    Note that for parsing rawValue into parsedValue (case 3) the parseStringValue() method is used<br>
     *                    Note that the implementation allows that rawValue may also contain the operator escape symbol ":"
     */
    protected void parseString(String stringValue) {

        // try to get operator and parsed Value by splitting from "operator:parsedValue"
        String[] splitted = stringValue.split(":", 2);

        // if operator is missing, fallback to the default operator "EQUALS" and use stringValue as parsedValue
        if(splitted.length == 1) {
            this.operator = Operator.EQUALS;
            this.parsedValue = stringValue;
            // TODO add operator parsing here too, since it is needed for unary operators

        // if the operator escape char ":" was used once or more than once, try to parse the first part as operator
        } else if(splitted.length >= 2) {
            String value;

            // if operator could be parsed, join the remaining parts back together as value and parse it to an Object
            //  this makes it possible to test for Strings containing an operator and also ":" symbols in the value
            try {
                this.operator = Operator.getOperator(splitted[0]);
                value = String.join(":", Arrays.copyOfRange(splitted, 1, splitted.length));
                this.parsedValue = parseStringValue(value);

            // if operator could not be parsed, use "EQUALS" as fallback operator and stringValue as parsedValue
            //  this makes it possible to test for Strings containing no operator but ":" symbols in the value
            } catch (IllegalArgumentException e) {
                this.operator = Operator.EQUALS;
                this.parsedValue = stringValue;

                // TODO notify about the fallback, so test writer knows
                //  test writer may have a typo in operator instead of testing for a legit String with : symbols
                //  print smt. like "Operator X unknown, testing via EQUALS with String X"
                //  another option is to simply forbid more than one ":" symbol in test values (as in prior version)
            }



        // if no case matched, throw an exception. Only happens if stringValue was null
        } else {
            throw new IllegalArgumentException("The value " + stringValue + " can not be parsed.");
        }
    }

    /**
     * parses a String containing a value into the according Object type for the value<br>
     *      Supported object types are:
     *      <ul><li>{@link java.lang.Number}</li>
     *      <li>{@link java.lang.String}, if no other Object type matched</li></ul>
     * @param stringValue String, value that should be parsed into an according Object type
     * @return Number or String, returns the parsed Object<br>
     *         If stringValue could not be parsed into an according Object type, it is returned as String
     */
    protected Object parseStringValue(String stringValue) {

        // TODO parse more than just numbers, since we have more operators for comparisons defined
        //  datetime, uri, url, boolean
        if(NumberUtils.isDigits(stringValue)) {
            return NumberUtils.createNumber(stringValue); //TODO why not using parseNumber() here as in eval()?
        } else {
            return stringValue;
        }
    }

    /**
     * parses a value Object into a BigDecimal number
     * @param value Object or null, the value Object that should be parsed <br>
     *              Can be of class Number or of class String <br>
     *              Returns null
     * @return BigDecimal the number parsed from value
     */
    BigDecimal parseNumber(Object value) {

        // if value is number parse and return it, if value is String and convertible into a number parse and return it
        if(value instanceof Number) {
            return new BigDecimal(value.toString());
        } else if (value instanceof String && NumberUtils.isCreatable((String) value)) {
            return new BigDecimal((String) value);
        }

        // if value cannot be parsed into a number, return null
        return null;
    }

    /**
     * Compare the actual value to the parsed value with the operator
     * TODO document available Operators and how they are evaluated
     *
     * @param actual Object, a value that should be parsed and the compared to parsedValue with operator
     * @return Boolean, returns whether the comparison is successful or not <br>
     *      e.g. actual: "test" will be successful for operator {@link Operator#EQUALS} and parsedValue "test" <br>
     *      e.g. actual: 3 will not be successful for operator {@link Operator#GREATER} and parsedValue 5
     */
    public boolean eval(Object actual) {

        // TODO parse more than just numbers, since we have more operators for comparisons defined
        //  datetime, uri, url, boolean
        // TODO Parsing has to be done with same logic as in parseRawValue() / parseString() to ensure comparability
        BigDecimal actualNumber = parseNumber(actual);
        BigDecimal currentNumber = parseNumber(parsedValue);

        // compare the actual value with the parsedValue based on operator
        switch (operator) {

            // equals compares semantic equivalence
            case EQUALS: {
                if (actualNumber != null && currentNumber != null) {
                    return actualNumber.compareTo(currentNumber) == 0;
                } else {
                    return parsedValue.equals(actual);
                }
            }

            // equalsIgnoreCase compares semantic equivalence, but for Strings ignoring the case
            case EQUALS_IGNORE_CASE: {
                if (actualNumber != null && currentNumber != null) {
                    return actualNumber.compareTo(currentNumber) == 0;
                } else {
                    return parsedValue.toString().equalsIgnoreCase(actual.toString());
                }
            }

            // contains(ignoreCase) compares if String representation of actual contains String representation of parsedValues (ignoring the case)
            case CONTAINS:
                return actual.toString().contains(parsedValue.toString());
            case CONTAINS_IGNORE_CASE:
                return actual.toString().toLowerCase().contains(parsedValue.toString().toLowerCase());

            // startsWith(ignoreCase) compares if String representation of actual starts with String representation of parsedValues (ignoring the case)
            case STARTS_WITH:
                return actual.toString().startsWith(parsedValue.toString());
            case STARTS_WITH_IGNORE_CASE:
                return actual.toString().toLowerCase().startsWith(parsedValue.toString().toLowerCase());

            // isDateTime uses qsf.commons.util.DateUtil to check if actual can be parsed into a Date Object
            case IS_DATE_TIME: {
                try {
                    DateUtil.getDate((String) actual);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            // TODO comment about on which implementation/standard the check of URI and URL is based
            case IS_URI: {
                throw new IllegalArgumentException("The operator " + operator.getCode() + " is not implemented.");
                // TODO implement
            }
            case IS_URL: {
                throw new IllegalArgumentException("The operator " + operator.getCode() + " is not implemented.");
                // TODO implement
            }

            // isBoolean checks if actual exactly matches one of the Strings "true" or "false" as in JSON standard
            case IS_BOOLEAN: {
                if ("true".equals(actual.toString()) || "false".equals(actual.toString())) {
                    return true;
                } else {
                    return false;
                }
            }

            // TODO comment about on which implementation/standard the check is based
            case IS_NUMBER: {
                throw new IllegalArgumentException("The operator " + operator.getCode() + " is not implemented.");
                // TODO
            }
            case IS_STRING: {
                throw new IllegalArgumentException("The operator " + operator.getCode() + " is not implemented.");
                // TODO
            }

            // exists is always true
            case EXISTS:
                return true;

            // greater(Equals) and less(Equals) compare numbers numerically and Strings lexicographically
            case GREATER: {
                if (actualNumber != null && currentNumber != null) {
                    return actualNumber.compareTo(currentNumber) > 0;
                } else {
                    return actual.toString().compareTo(parsedValue.toString()) > 0;
                }
            }
            case GREATER_EQUALS: {
                if (actualNumber != null && currentNumber != null) {
                    return actualNumber.compareTo(currentNumber) >= 0;
                } else {
                    return actual.toString().compareTo(parsedValue.toString()) >= 0;
                }
            }
            case LESS: {
                if (actualNumber != null && currentNumber != null) {
                    return actualNumber.compareTo(currentNumber) < 0;
                } else {
                    return actual.toString().compareTo(parsedValue.toString()) < 0;
                }
            }
            case LESS_EQUALS: {
                if (actualNumber != null && currentNumber != null) {
                    return actualNumber.compareTo(currentNumber) <= 0;
                } else {
                    return actual.toString().compareTo(parsedValue.toString()) <= 0;
                }
            }
            default:
                // the default case is used when there was an operator defined but not implemented yet
                throw new IllegalArgumentException("The operator " + operator.getCode() + " is not implemented.");

        }
    }


    /**
     * Getter for property 'operator'.
     *
     * @return Value for property 'operator'.
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * Getter for property 'parsedValue'.
     *
     * @return Value for property 'parsedValue'.
     */
    public Object getParsedValue() {
        return parsedValue;
    }
}
