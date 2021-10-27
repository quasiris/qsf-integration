package com.quasiris.qsf.test.service;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.quasiris.qsf.TestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class JsonPathParserTest {

    @Test
    void getValueName() throws IOException {
        Object value = TestHelper.getResourceAsObject("/com/quasiris/qsf/test/JsonPathParser/example.json");
        String actual = JsonPathParser.getValue("$.name", value);
        Assertions.assertEquals("Alexander", actual);
    }

    @Test
    void getValueDetailsComplex() throws IOException {
        Object value = TestHelper.getResourceAsObject("/com/quasiris/qsf/test/JsonPathParser/example.json");
        String actual = JsonPathParser.getValue("$.details.firstCar", value);
        Assertions.assertEquals("Mercedes", actual);
    }

    @Test
    void getValueNullInputNullResult() {
        String actual = JsonPathParser.getValue("$.name", null);
        Assertions.assertNull(actual);
    }

    @Test
    void getValueFromContext() throws IOException {
        String fromFile = TestHelper.getResourceAsString("/com/quasiris/qsf/test/JsonPathParser/example.json");
        DocumentContext value = JsonPath.parse(fromFile);
        String actual = JsonPathParser.getValueFromContext("$.name", value);
        Assertions.assertEquals("Alexander", actual);
    }

    @Test
    void getValueFromContextNullInputNullResult() throws IOException {
        String actual = JsonPathParser.getValueFromContext("$.name", null);
        Assertions.assertNull(actual);
    }

    @Test
    void getValueFromContextDetailsComplex() throws IOException {
        String fromFile = TestHelper.getResourceAsString("/com/quasiris/qsf/test/JsonPathParser/example.json");
        DocumentContext value = JsonPath.parse(fromFile);
        String actual = JsonPathParser.getValueFromContext("$.details.firstCar", value);
        Assertions.assertEquals("Mercedes", actual);
    }

    @Test
    void getDocumentContext() throws IOException {
        String value = TestHelper.getResourceAsString("/com/quasiris/qsf/test/JsonPathParser/example.json");
        DocumentContext documentContext = JsonPathParser.getDocumentContext(value);
        Assertions.assertNotNull(documentContext);
    }
}