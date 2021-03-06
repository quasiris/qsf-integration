package com.quasiris.qsf.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.HashMap;
import java.util.Map;

public class JsonBuilderTest {


    @Test
    public void testEmptyObject() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object();
        JSONAssert.assertEquals("{}", jsonBuilder.writeAsString(), true);
    }

    @Test
    public void testEmptyArray() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.array();
        JSONAssert.assertEquals("[]", jsonBuilder.writeAsString(), true);
    }

    @Test
    public void testArray() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object();
        jsonBuilder.array("foo");
        JSONAssert.assertEquals("{\"foo\" : []}", jsonBuilder.writeAsString(), true);
    }

    @Test
    public void testObject() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object();
        jsonBuilder.object("foo");
        JSONAssert.assertEquals("{\"foo\" : {}}", jsonBuilder.writeAsString(), true);
    }

    @Test
    public void testObjectWithArray() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.array();
        jsonBuilder.object("foo");
        JSONAssert.assertEquals("[{\"foo\" : {}}]", jsonBuilder.writeAsString(), true);
    }


    @Test
    public void testArrayEmptyObject() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object();
        jsonBuilder.array("foo");
        jsonBuilder.object();
        JSONAssert.assertEquals("{\"foo\":[{}]}", jsonBuilder.writeAsString(), true);
    }

    @Test
    public void testArrayWithObject() throws Exception {
        Assertions.assertThrows(JsonBuilderException.class, () -> {
            JsonBuilder jsonBuilder = new JsonBuilder();
            jsonBuilder.object();
            jsonBuilder.array();

        });
    }

    @Test
    public void testArrayWithArray() throws Exception {
        Assertions.assertThrows(JsonBuilderException.class, () -> {
            JsonBuilder jsonBuilder = new JsonBuilder();
            jsonBuilder.array();
            jsonBuilder.array();

        });

    }

    @Test
    public void testArrayWithArrayWithFieldname() throws Exception {
        Assertions.assertThrows(JsonBuilderException.class, () -> {
            JsonBuilder jsonBuilder = new JsonBuilder();
            jsonBuilder.array();
            jsonBuilder.array("foo");
        });

    }

    @Test
    public void testObjectEmptyObjectException() throws Exception {
        Assertions.assertThrows(JsonBuilderException.class, () -> {
            JsonBuilder jsonBuilder = new JsonBuilder();
            jsonBuilder.object();
            jsonBuilder.object("foo");
            jsonBuilder.object();
        });
    }

    @Test
    public void testString() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.string("{\"foo\" : {}}");
        JSONAssert.assertEquals("{\"foo\" : {}}", jsonBuilder.writeAsString(), true);
    }


    @Test
    public void testStringWithBrokenJson() throws Exception {
        Assertions.assertThrows(JsonBuilderException.class, () -> {
            JsonBuilder jsonBuilder = new JsonBuilder();
            jsonBuilder.string("{\"foo\" : {}");
        });
    }


    @Test
    public void testStringWithFieldname() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.string("test", "{\"foo\" : {}}");
        JSONAssert.assertEquals("{\"test\":{\"foo\":{}}}", jsonBuilder.writeAsString(), true);
    }

    @Test
    public void testStringWithFieldnameWithBrokenJson() throws Exception {
        Assertions.assertThrows(JsonBuilderException.class, () -> {
            JsonBuilder jsonBuilder = new JsonBuilder();
            jsonBuilder.string("test", "{\"foo\" : {}");
        });
    }

    @Test
    public void testAddString() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.addString("{\"foo\" : {}}");
        JSONAssert.assertEquals("[{\"foo\" : {}}]", jsonBuilder.writeAsString(), true);
    }


    @Test
    public void testAddStringWithBrokenJson() throws Exception {
        Assertions.assertThrows(JsonBuilderException.class, () -> {
            JsonBuilder jsonBuilder = new JsonBuilder();
            jsonBuilder.addString("{\"foo\" : {}");
        });
    }


    @Test
    public void testPojo() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        TestPojo testPojo = new TestPojo("bar");
        jsonBuilder.pojo(testPojo);
        JSONAssert.assertEquals("{\"foo\" : \"bar\"}", jsonBuilder.writeAsString(), true);
    }

    @Test
    public void testPojoWithKey() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        TestPojo testPojo = new TestPojo("bar");
        jsonBuilder.pojo("test", testPojo);
        JSONAssert.assertEquals("{\"test\":{\"foo\":\"bar\"}}", jsonBuilder.writeAsString(), true);
    }


    @Test
    public void testAddPojo() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.array();

        TestPojo testPojo = new TestPojo("bar");
        jsonBuilder.addPojo(testPojo);
        JSONAssert.assertEquals("[{\"foo\" : \"bar\"}]", jsonBuilder.writeAsString(), true);
    }

    @Test
    public void testPojoWithObject() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        TestPojo testPojo = new TestPojo("bar");
        jsonBuilder.object().pojo(testPojo);
        JSONAssert.assertEquals("{\"foo\" : \"bar\"}", jsonBuilder.writeAsString(), true);
    }

    @Test
    public void testShashUnstash() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();

        jsonBuilder.
                object("foo").
                stash().
                object("bar").
                unstash().
                object("test");
        JSONAssert.assertEquals("{\"foo\": { \"bar\": {}, \"test\": {} }}", jsonBuilder.writeAsString(), true);
    }

    @Test
    public void testShashUnstashWithKey() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();

        jsonBuilder.
                object("foo").
                stash("foo").
                object("bar").
                unstash("foo").
                object("test");
        JSONAssert.assertEquals("{\"foo\": { \"bar\": {}, \"test\": {} }}", jsonBuilder.writeAsString(), true);
    }

    @Test
    public void testRoot() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();

        jsonBuilder.
                object("foo").
                object("bar").
                root().
                object("test");

        JSONAssert.assertEquals("{\"foo\": { \"bar\": {}}, \"test\" : {}}", jsonBuilder.writeAsString(), true);
    }

    @Test
    public void testCurrent() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();

        JsonNode jsonNode = jsonBuilder.
                object("foo").
                object("bar").
                root().
                object("test").
                getCurrent();

        ObjectMapper mapper = new ObjectMapper();
        String expected = mapper.writeValueAsString(jsonNode);
        JSONAssert.assertEquals("{}", expected, true);
    }

    @Test
    public void testGet() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();

        JsonNode jsonNode = jsonBuilder.
                object("foo").
                object("bar").
                get();

        ObjectMapper mapper = new ObjectMapper();
        String expected = mapper.writeValueAsString(jsonNode);
        JSONAssert.assertEquals("{\"foo\":{\"bar\":{}}}", expected, true);
    }

    @Test
    public void testJson() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();

        JsonNode jsonNode = jsonBuilder.
                object("foo").
                root().
                getCurrent();

        jsonBuilder.object("bar").json(jsonNode);

        JSONAssert.assertEquals("{\"foo\":{},\"bar\":{\"foo\":{}}}", jsonBuilder.writeAsString(), true);
    }


    @Test
    public void testJsonWithArray() throws Exception {
        Assertions.assertThrows(JsonBuilderException.class, () -> {
            JsonBuilder jsonBuilder = new JsonBuilder();

            JsonNode jsonNode = jsonBuilder.
                    object("foo").
                    root().
                    getCurrent();

            jsonBuilder.array("bar").json(jsonNode);
        });
    }


    @Test
    public void testAddJson() throws Exception {
        JsonBuilder jsonBuilderTest = new JsonBuilder();

        JsonNode jsonNode = jsonBuilderTest.
                object("foo").
                root().
                getCurrent();

        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.array("arr").addJson(jsonNode);

        JSONAssert.assertEquals("{\"arr\":[{\"foo\":{}}]}", jsonBuilder.writeAsString(), true);
    }

    @Test
    public void testAddJsonWithObject() throws Exception {
        Assertions.assertThrows(JsonBuilderException.class, () -> {
            JsonBuilder jsonBuilderTest = new JsonBuilder();

            JsonNode jsonNode = jsonBuilderTest.
                    object("foo").
                    root().
                    getCurrent();

            JsonBuilder jsonBuilder = new JsonBuilder();
            jsonBuilder.object("arr").addJson(jsonNode);
        });
    }


    @Test
    public void testValue() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object("foo", "bar");
        JSONAssert.assertEquals("{\"foo\":\"bar\"}", jsonBuilder.writeAsString(), true);
    }

    @Test
    public void testValueWithNullKey() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object().object(null, "bar");
        JSONAssert.assertEquals("{}", jsonBuilder.writeAsString(), true);
    }

    @Test
    public void testAddValue() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.array().addValue("foo");
        JSONAssert.assertEquals("[\"foo\"]", jsonBuilder.writeAsString(), true);
    }

    @Test
    public void testPath() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object("n1").object("n2").object("n3").object("n4");
        jsonBuilder.root();
        jsonBuilder.path("n1").path("n2");
        jsonBuilder.object("n33");
        JSONAssert.assertEquals("{\"n1\":{\"n2\":{\"n3\":{\"n4\":{}},\"n33\":{}}}}", jsonBuilder.writeAsString(), true);
    }

    @Test
    public void testExistsTrue() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object("n1").object("n2");
        jsonBuilder.root();
        boolean exists = jsonBuilder.exists("n1/n2");
        Assertions.assertTrue(exists);
    }

    @Test
    public void testExistsFalse() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object("n1").object("n2");
        jsonBuilder.root();
        boolean exists = jsonBuilder.exists("n1/n2/n3");
        Assertions.assertFalse(exists);
    }


    @Test
    public void testPathsForceCreate() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object("n1").object("n2");
        jsonBuilder.root();
        jsonBuilder.pathsForceCreate("n1/n2/n3/n4");
        jsonBuilder.object("n33");
        JSONAssert.assertEquals("{\"n1\":{\"n2\":{\"n3\":{\"n4\":{\"n33\":{}}}}}}", jsonBuilder.writeAsString(), true);
    }


    @Test
    public void testPaths() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object("n1").object("n2").object("n3").object("n4");
        jsonBuilder.root();
        jsonBuilder.paths("n1/n2");
        jsonBuilder.object("n33");
        JSONAssert.assertEquals("{\"n1\":{\"n2\":{\"n3\":{\"n4\":{}},\"n33\":{}}}}", jsonBuilder.writeAsString(), true);
    }


    @Test
    public void testPathsNotExists() throws Exception {
        Assertions.assertThrows(JsonBuilderException.class, () -> {
            JsonBuilder jsonBuilder = new JsonBuilder();
            jsonBuilder.object("n1").object("n2").object("n3").object("n4");
            jsonBuilder.root();
            jsonBuilder.paths("n1/n2/n4");
        });

    }

    @Test
    public void testReplace() throws Exception {
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("$foo", "bar");

        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object("$foo", "bar");
        jsonBuilder.replace(valueMap);

        JSONAssert.assertEquals("{\"bar\": \"bar\"}", jsonBuilder.writeAsString(), true);
    }

}
