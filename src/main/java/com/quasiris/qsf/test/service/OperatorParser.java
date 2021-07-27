package com.quasiris.qsf.test.service;

import com.quasiris.qsf.util.DateUtil;
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

        switch (operator) {
            case EQUALS:
                if (actualNumer != null && currentNumber != null) {
                    return actualNumer.compareTo(currentNumber) == 0;
                } else {
                    return parsedValue.equals(actual);
                }
            case GREATER:
                if (actualNumer != null && currentNumber != null) {
                    return actualNumer.compareTo(currentNumber) > 0;
                } else {
                    return actual.toString().compareTo(parsedValue.toString()) > 0;
                }
            case LESS:
                if (actualNumer != null && currentNumber != null) {
                    return actualNumer.compareTo(currentNumber) < 0;
                } else {
                    return actual.toString().compareTo(parsedValue.toString()) < 0;
                }
            case GREATER_EQUALS:
                if (actualNumer != null && currentNumber != null) {
                    return actualNumer.compareTo(currentNumber) >= 0;
                } else {
                    return actual.toString().compareTo(parsedValue.toString()) >= 0;
                }
            case LESS_EQUALS:
                if (actualNumer != null && currentNumber != null) {
                    return actualNumer.compareTo(currentNumber) <= 0;
                } else {
                    return actual.toString().compareTo(parsedValue.toString()) <= 0;
                }
            case STARTS_WITH:
               return actual.toString().startsWith(parsedValue.toString());
            case STARTS_WITH_IGNORE_CASE:
                return actual.toString().toLowerCase().startsWith(parsedValue.toString().toLowerCase());
            case CONTAINS:
               return actual.toString().contains(parsedValue.toString());
            case CONTAINS_IGNORE_CASE:
                return actual.toString().toLowerCase().contains(parsedValue.toString().toLowerCase());
            case IS_DATE_TIME:
                try {
                    DateUtil.getDate((String) actual);
                    return true;
                } catch (Exception e) {
                   return false;
                }
            case IS_BOOLEAN:
                if("true".equals(actual.toString()) || "false".equals(actual.toString())) {
                    return true;
                } else {
                    return false;
                }
            default:
                throw new IllegalArgumentException("The operator " + operator.getCode() + " is not implemented.");

        }
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
