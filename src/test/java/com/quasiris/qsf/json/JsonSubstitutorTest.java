package com.quasiris.qsf.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.quasiris.qsf.test.json.JsonAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        valueMap.put("foo", "bar");

        JsonSubstitutor jsonSubstitutor = new JsonSubstitutor(valueMap);
        JsonNode json = jsonSubstitutor.replace(jsonBuilder.get());

        JsonAssert.assertJson("{\"bar\": \"bar\"}", json);

    }

    @Test
    public void testReplaceValue() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object("foo", "$bar");

        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("bar", "foo");

        JsonSubstitutor jsonSubstitutor = new JsonSubstitutor(valueMap);
        JsonNode json = jsonSubstitutor.replace(jsonBuilder.get());

        JsonAssert.assertJson("{\"foo\": \"foo\"}", json);

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
        valueMap.put("varSimple", "drei");
        valueMap.put("varArray", Arrays.asList("fünf", "sechs"));
        valueMap.put("number", 8);

        JsonSubstitutor jsonSubstitutor = new JsonSubstitutor(valueMap);
        JsonNode json = jsonSubstitutor.replace(jsonBuilder.get());

        JsonAssert.assertJson(
                "{\"foo\": \"bar\", \"array\" : [\"eins\", \"zwei\", \"drei\", \"vier\", \"fünf\", \"sechs\", \"sieben\", 8]}",
                json);

    }



    @Test
    public void testNestedReplaceArray() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.classpath("com/quasiris/qsf/json/test-nested-replace-array.json");

        jsonBuilder.valueMap("foo", "bar");
        jsonBuilder.replace();

        JsonAssert.assertJson(
                "{ \"must\" : [ { \"foo\" : \"bar\", \"bool\" : { } } ] }",
                jsonBuilder.get());
    }

    @Test
    public void testdReplaceObjectNode() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.classpath("com/quasiris/qsf/json/test-replace-object-node.json");

        JsonBuilder replaceBuilder = new JsonBuilder();
        replaceBuilder.
                object("foo", "bar");

        jsonBuilder.valueMap("replaceMe", replaceBuilder.get());
        jsonBuilder.replace();

        JsonAssert.assertJson(
                "{\"alice\" : \"bob\", \"foo\" : \"bar\"}",
                jsonBuilder.get());
    }

    @Test
    public void testdReplaceObjectNodeWithMultipleKeys() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.classpath("com/quasiris/qsf/json/test-replace-object-node.json");

        JsonNode replaceMe = JsonBuilder.create().
                classpath("com/quasiris/qsf/json/test-replace-object-node-with-multiple-keys.json").get();

        jsonBuilder.valueMap("replaceMe", replaceMe);
        jsonBuilder.replace();

        JsonAssert.assertJson(
                "{\"alice\" : \"bob\", \"eins\" : \"1\", \"zwei\" : \"2\", \"drei\" : \"3\"}",
                jsonBuilder.get());
    }

    @Test
    public void testdReplaceObjectNodeWithNoKey() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.classpath("com/quasiris/qsf/json/test-replace-object-node.json");

        JsonNode replaceMe = JsonBuilder.create().string("{}").get();

        jsonBuilder.valueMap("replaceMe", replaceMe);
        jsonBuilder.replace();

        JsonAssert.assertJson(
                "{\"alice\" : \"bob\"}",
                jsonBuilder.get());
    }

    @Test
    public void testdRemoveObjectNode() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.classpath("com/quasiris/qsf/json/test-remove-object-node.json");

        jsonBuilder.valueMap("removeMe", new EmptyNode());
        jsonBuilder.replace();

        JsonAssert.assertJson(
                "{\"alice\" : \"bob\"}",
                jsonBuilder.get());
    }


}
