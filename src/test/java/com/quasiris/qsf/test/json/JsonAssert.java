package com.quasiris.qsf.test.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasiris.qsf.commons.util.IOUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonAssert {



    public static void assertJsonFile(String expectedFile, JsonNode json) throws IOException {
        String expected = IOUtils.getString(expectedFile);
        assertJson(expected, json);
    }

    public static void assertJson(String expected, JsonNode json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String query = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);

        JsonNode expectedJson = objectMapper.readValue(expected, JsonNode.class);
        expected = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(expectedJson);
        assertEquals(expected, query);
    }
}
