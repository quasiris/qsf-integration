package com.quasiris.qsf.test.json;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.quasiris.qsf.commons.util.IOUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonAssert {



    public static void assertJsonFile(String expectedFile, JsonNode json) throws IOException {
        String expected = IOUtils.getString(expectedFile);
        assertJson(expected, json);
    }

    public static void assertJson(String expected, Object json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

        String query = objectMapper.writer(prettyPrinter).writeValueAsString(json);

        JsonNode expectedJson = objectMapper.readValue(expected, JsonNode.class);
        expected = objectMapper.writer(prettyPrinter).writeValueAsString(expectedJson);
        assertEquals(expected, query);
    }
}
