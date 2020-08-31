package com.quasiris.qsf.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class JsonSubstitutorTest {


    @Test
    public void testNull() throws Exception {
        JsonSubstitutor jsonSubstitutor = new JsonSubstitutor(null);
        JsonNode json = jsonSubstitutor.replace(null);
        Assert.assertNull(json);
    }

    @Test
    public void testReplaceKey() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object("$foo", "bar");

        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("$foo", "bar");

        JsonSubstitutor jsonSubstitutor = new JsonSubstitutor(valueMap);
        JsonNode json = jsonSubstitutor.replace(jsonBuilder.get());

        JSONAssert.assertEquals("{\"bar\": \"bar\"}", json.toString(), true);

    }

    @Test
    public void testReplaceValue() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object("foo", "$bar");

        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("$bar", "foo");

        JsonSubstitutor jsonSubstitutor = new JsonSubstitutor(valueMap);
        JsonNode json = jsonSubstitutor.replace(jsonBuilder.get());

        JSONAssert.assertEquals("{\"foo\": \"foo\"}", json.toString(), true);

    }

    @Test
    public void testReplaceArray() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.
                object("foo", "bar").
                array("array").
                addValue("eins").
                addValue("zwei").
                addValue("$varSimple").
                addValue("vier").
                addValue("$varArray").
                addValue("sieben").
                addValue("$number");



        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("$varSimple", "drei");
        valueMap.put("$varArray", Arrays.asList("fünf", "sechs"));
        valueMap.put("$number", 8);

        JsonSubstitutor jsonSubstitutor = new JsonSubstitutor(valueMap);
        JsonNode json = jsonSubstitutor.replace(jsonBuilder.get());

        JSONAssert.assertEquals(
                "{\"foo\": \"bar\", \"array\" : [\"eins\", \"zwei\", \"drei\", \"vier\", \"fünf\", \"sechs\", \"sieben\", 8]}",
                json.toString(),
                true);

    }


}
