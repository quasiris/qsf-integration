package com.quasiris.qsf.test.service;

import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;

public class OperatorParser {

    private Object rawValue;
    private Object parsedValue;

    private Operator operator;

    private boolean parsed = false;

    public OperatorParser(Object rawValue) {
        this.rawValue = rawValue;
    }

    public void parse() {
        if(rawValue instanceof String) {
            parseString((String) rawValue);
        }
        parsed = true;
    }

    protected void parseString(String stringValue) {
        String[] splitted = stringValue.split(":");
        if(splitted.length > 1) {
            this.operator = Operator.getOperator(splitted[0]);
            // TODO handle situation whit a : in the parsedValue
            String value = splitted[1];
            if(NumberUtils.isDigits(value)) {
                this.parsedValue = NumberUtils.createNumber(value);
            } else {
                this.parsedValue = splitted[1];
            }

        } else {
            this.operator = Operator.EQUALS;
            this.parsedValue = stringValue;
        }
    }

    public boolean eval(Object actual) {
        if(!parsed) {
            parse();
        }

        BigDecimal actualNumer = parseNumber(actual);
        BigDecimal currentNumber = parseNumber(parsedValue);

        if(operator.isEquals()) {
            if (actualNumer != null && currentNumber != null) {
                return actualNumer.compareTo(currentNumber) == 0;
            } else {
                return parsedValue.equals(actual);
            }
        } else if(operator.isGreater()) {
            if (actualNumer != null && currentNumber != null) {
                return actualNumer.compareTo(currentNumber) > 0;
            } else {
                return actual.toString().compareTo(parsedValue.toString()) > 0;
            }
        } else if(operator.isLess()) {
            if (actualNumer != null && currentNumber != null) {
                return actualNumer.compareTo(currentNumber) < 0;
            } else {
                return actual.toString().compareTo(parsedValue.toString()) < 0;
            }
        } else if(operator.isGreaterEquals()) {
            if (actualNumer != null && currentNumber != null) {
                return actualNumer.compareTo(currentNumber) >= 0;
            } else {
                return actual.toString().compareTo(parsedValue.toString()) >= 0;
            }
        } else if(operator.isLessEquals()) {
            if (actualNumer != null && currentNumber != null) {
                return actualNumer.compareTo(currentNumber) <= 0;
            } else {
                return actual.toString().compareTo(parsedValue.toString()) <= 0;
            }
        }

        return false;
    }

    BigDecimal parseNumber(Object value) {
        if(value instanceof Number) {
            return new BigDecimal(value.toString());
        } else if (value instanceof String && NumberUtils.isCreatable((String) value)) {
            return new BigDecimal((String) value);
        }
        return null;

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
