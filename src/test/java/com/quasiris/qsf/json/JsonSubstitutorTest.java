package com.quasiris.qsf.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class JsonSubstitutorTest {


    @Test
    public void testNull() throws Exception {
        JsonSubstitutor jsonSubstitutor = new JsonSubstitutor(null);
        JsonNode json = jsonSubstitutor.replace(null);
        Assertions.assertNull(json);
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



    @Test
    public void testNestedReplaceArray() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.classpath("com/quasiris/qsf/json/test-nested-replace-array.json");

        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("$foo", "bar");

        jsonBuilder.replace(valueMap);

        JSONAssert.assertEquals(
                "{ \"must\" : [ { \"foo\" : \"bar\", \"bool\" : { } } ] }",
                jsonBuilder.writeAsString(),
                true);
    }

    @Test
    public void testdReplaceObjectNode() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.classpath("com/quasiris/qsf/json/test-replace-object-node.json");

        JsonBuilder replaceBuilder = new JsonBuilder();
        replaceBuilder.
                object("foo", "bar");


        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("$replaceMe", replaceBuilder.get());

        jsonBuilder.replace(valueMap);

        JSONAssert.assertEquals(
                "{\"alice\" : \"bob\", \"foo\" : \"bar\"}",
                jsonBuilder.writeAsString(),
                true);
    }

    @Test
    public void testdReplaceObjectNodeWithMultipleKeys() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.classpath("com/quasiris/qsf/json/test-replace-object-node.json");

        JsonNode replaceMe = JsonBuilder.create().
                classpath("com/quasiris/qsf/json/test-replace-object-node-with-multiple-keys.json").get();

        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("$replaceMe", replaceMe);

        jsonBuilder.replace(valueMap);

        JSONAssert.assertEquals(
                "{\"alice\" : \"bob\", \"eins\" : \"1\", \"zwei\" : \"2\", \"drei\" : \"3\"}",
                jsonBuilder.writeAsString(),
                true);
    }

    @Test
    public void testdReplaceObjectNodeWithNoKey() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.classpath("com/quasiris/qsf/json/test-replace-object-node.json");

        JsonNode replaceMe = JsonBuilder.create().string("{}").get();


        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("$replaceMe", replaceMe);

        jsonBuilder.replace(valueMap);

        JSONAssert.assertEquals(
                "{\"alice\" : \"bob\"}",
                jsonBuilder.writeAsString(),
                true);
    }

    @Test
    public void testdRemoveObjectNode() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.classpath("com/quasiris/qsf/json/test-remove-object-node.json");

        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("$removeMe", new EmptyNode());

        jsonBuilder.replace(valueMap);

        JSONAssert.assertEquals(
                "{\"alice\" : \"bob\"}",
                jsonBuilder.writeAsString(),
                true);
    }


}
