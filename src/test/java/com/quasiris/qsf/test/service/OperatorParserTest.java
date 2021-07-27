package com.quasiris.qsf.test.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OperatorParserTest {

    @Test
    public void testDefaultOperatorWithString() {
        OperatorParser parser = new OperatorParser("foo");
        assertTrue(parser.eval("foo"));

    }

    @Test
    public void testEqualstOperatorWithString() {
        OperatorParser parser = new OperatorParser("equals:foo");
        assertTrue(parser.eval("foo"));
    }

    @Test
    public void testEqualstOperatorWithStringFalse() {
        OperatorParser parser = new OperatorParser("equals:foo");
        assertFalse(parser.eval("bar"));
    }

    @Test
    public void testStartsWithOperator() {
        OperatorParser parser = new OperatorParser("startsWith:https://");
        assertTrue(parser.eval("https://www.quasiris.de"));
    }

    @Test
    public void testStartsWithOperatorFalse() {
        OperatorParser parser = new OperatorParser("startsWith:https://");
        assertFalse(parser.eval("HTTPS://www.quasiris.de"));
    }

    @Test
    public void testStartsWithIgnoreCaseOperator() {
        OperatorParser parser = new OperatorParser("startsWithIgnoreCase:https://");
        assertTrue(parser.eval("HTTPS://www.quasiris.de"));
    }

    @Test
    public void testStartsWithIgnoreCaseOperatorFalse() {
        OperatorParser parser = new OperatorParser("startsWithIgnoreCase:https://");
        assertFalse(parser.eval("file://www.quasiris.de"));
    }

    @Test
    public void testContainsOperator() {
        OperatorParser parser = new OperatorParser("contains:quasiris");
        assertTrue(parser.eval("https://www.quasiris.de"));
    }

    @Test
    public void testContainsOperatorFalse() {
        OperatorParser parser = new OperatorParser("contains:quasiris");
        assertFalse(parser.eval("https://www.Quasris.de"));
    }

    @Test
    public void testContainsIgnoreCaseOperator() {
        OperatorParser parser = new OperatorParser("containsIgnoreCase:quasiris");
        assertTrue(parser.eval("HTTPS://www.Quasiris.de"));
    }

    @Test
    public void testContainsIgnoreCaseOperatorFalse() {
        OperatorParser parser = new OperatorParser("containsIgnoreCase:quasiris");
        assertFalse(parser.eval("HTTPS://www.foo.de"));
    }

    @Test
    public void testIsDateTimeOperator() {
        OperatorParser parser = new OperatorParser("isDateTime:2021-07-22T03:46:16.467+0000");
        assertTrue(parser.eval("2021-12-22T03:46:16.467+0000"));
    }

    @Test
    public void testIsBooleanTrueOperator() {
        OperatorParser parser = new OperatorParser("isBoolean:true");
        assertTrue(parser.eval("true"));
    }

    @Test
    public void testIsBooleanFalseOperator() {
        OperatorParser parser = new OperatorParser("isBoolean:false");
        assertTrue(parser.eval("false"));
    }

    @Test
    public void testIsDateTimeOperatorFalse() {
        OperatorParser parser = new OperatorParser("isDateTime:2021-07-22T03:46:16.467+0000");
        assertFalse(parser.eval("noDate"));
    }

    @Test
    public void testEqualstSymbolOperatorWithString() {
        OperatorParser parser = new OperatorParser("=:foo");
        assertTrue(parser.eval("foo"));
    }

    @Test
    public void testEqualstSymbolOperatorWithStringFalse() {
        OperatorParser parser = new OperatorParser("=:foo");
        assertFalse(parser.eval("bar"));
    }

    @Test
    public void testInvalidOperator() {
        assertThrows(IllegalArgumentException.class, () -> {
            OperatorParser parser = new OperatorParser("invalidOperator:foo");
            parser.eval("foo");
        });

    }

    @Test
    public void testEqualstOperatorWithLong() {
        OperatorParser parser = new OperatorParser("equals:4200");
        assertTrue(parser.eval(4200.0));
        assertTrue(parser.eval(4200L));
        assertTrue(parser.eval(4200));
        assertTrue(parser.eval("4200"));
    }


    @Test
    public void testGreaterSymboOperatorWithNumber() {
        OperatorParser parser = new OperatorParser(">:4200");
        assertTrue(parser.eval(4300.0));
        assertTrue(parser.eval(4300L));
        assertTrue(parser.eval(4300));
        assertTrue(parser.eval("4300"));
        assertFalse(parser.eval(4100.0));
        assertFalse(parser.eval(4100L));
        assertFalse(parser.eval(4100));
        assertFalse(parser.eval("4100"));
    }

    @Test
    public void testGreaterOperatorWithNumber() {
        OperatorParser parser = new OperatorParser("greater:4200");
        assertTrue(parser.eval(4300.0));
        assertTrue(parser.eval(4300L));
        assertTrue(parser.eval(4300));
        assertTrue(parser.eval("4300"));
        assertFalse(parser.eval(4100.0));
        assertFalse(parser.eval(4100L));
        assertFalse(parser.eval(4100));
        assertFalse(parser.eval("4100"));
    }

    @Test
    public void testLessSymboOperatorWithNumber() {
        OperatorParser parser = new OperatorParser("<:4200");
        assertTrue(parser.eval(4100.0));
        assertTrue(parser.eval(4100L));
        assertTrue(parser.eval(4100));
        assertTrue(parser.eval("4100"));
        assertFalse(parser.eval(4300.0));
        assertFalse(parser.eval(4300L));
        assertFalse(parser.eval(4300));
        assertFalse(parser.eval("4300"));
    }

    @Test
    public void testLessOperatorWithNumber() {
        OperatorParser parser = new OperatorParser("less:4200");
        assertTrue(parser.eval(4100.0));
        assertTrue(parser.eval(4100L));
        assertTrue(parser.eval(4100));
        assertTrue(parser.eval("4100"));
        assertFalse(parser.eval(4300.0));
        assertFalse(parser.eval(4300L));
        assertFalse(parser.eval(4300));
        assertFalse(parser.eval("4300"));
    }


    @Test
    public void testLessSymbolEqualsOperatorWithNumber() {
        OperatorParser parser = new OperatorParser("<=:4200");
        assertTrue(parser.eval(4100.0));
        assertTrue(parser.eval(4100L));
        assertTrue(parser.eval(4100));
        assertTrue(parser.eval("4100"));
        assertTrue(parser.eval("4200"));
        assertFalse(parser.eval(4300.0));
        assertFalse(parser.eval(4300L));
        assertFalse(parser.eval(4300));
        assertFalse(parser.eval("4300"));
    }

    @Test
    public void testLessEqualsOperatorWithNumber() {
        OperatorParser parser = new OperatorParser("lessequals:4200");
        assertTrue(parser.eval(4100.0));
        assertTrue(parser.eval(4100L));
        assertTrue(parser.eval(4100));
        assertTrue(parser.eval("4100"));
        assertTrue(parser.eval("4200"));
        assertFalse(parser.eval(4300.0));
        assertFalse(parser.eval(4300L));
        assertFalse(parser.eval(4300));
        assertFalse(parser.eval("4300"));
    }


    @Test
    public void testGreaterSymbolEqualsOperatorWithNumber() {
        OperatorParser parser = new OperatorParser(">=:4200");
        assertTrue(parser.eval(4300.0));
        assertTrue(parser.eval(4300L));
        assertTrue(parser.eval(4300));
        assertTrue(parser.eval("4300"));
        assertTrue(parser.eval("4200"));
        assertFalse(parser.eval(4100.0));
        assertFalse(parser.eval(4100L));
        assertFalse(parser.eval(4100));
        assertFalse(parser.eval("4100"));
    }

    @Test
    public void testGreaterEqualsOperatorWithNumber() {
        OperatorParser parser = new OperatorParser("greaterequals:4200");
        assertTrue(parser.eval(4300.0));
        assertTrue(parser.eval(4300L));
        assertTrue(parser.eval(4300));
        assertTrue(parser.eval("4300"));
        assertTrue(parser.eval("4200"));
        assertFalse(parser.eval(4100.0));
        assertFalse(parser.eval(4100L));
        assertFalse(parser.eval(4100));
        assertFalse(parser.eval("4100"));
    }


}
