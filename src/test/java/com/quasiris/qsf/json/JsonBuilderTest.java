package com.quasiris.qsf.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

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

    @Test(expected = JsonBuilderException.class)
    public void testObjectWithArray() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.array();
        jsonBuilder.object("foo");
    }


    @Test
    public void testArrayEmptyObject() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object();
        jsonBuilder.array("foo");
        jsonBuilder.object();
        JSONAssert.assertEquals("{\"foo\":[{}]}", jsonBuilder.writeAsString(), true);
    }

    @Test(expected = JsonBuilderException.class)
    public void testArrayWithObject() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object();
        jsonBuilder.array();
    }

    @Test(expected = JsonBuilderException.class)
    public void testArrayWithArray() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.array();
        jsonBuilder.array();
    }

    @Test(expected = JsonBuilderException.class)
    public void testArrayWithArrayWithFieldname() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.array();
        jsonBuilder.array("foo");
    }

    @Test(expected = JsonBuilderException.class)
    public void testObjectEmptyObjectException() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object();
        jsonBuilder.object("foo");
        jsonBuilder.object();
    }

    @Test
    public void testString() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.string("{\"foo\" : {}}");
        JSONAssert.assertEquals("{\"foo\" : {}}", jsonBuilder.writeAsString(), true);
    }


    @Test(expected = JsonBuilderException.class)
    public void testStringWithBrokenJson() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.string("{\"foo\" : {}");
    }


    @Test
    public void testStringWithFieldname() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.string("test", "{\"foo\" : {}}");
        JSONAssert.assertEquals("{\"test\":{\"foo\":{}}}", jsonBuilder.writeAsString(), true);
    }

    @Test(expected = JsonBuilderException.class)
    public void testStringWithFieldnameWithBrokenJson() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.string("test", "{\"foo\" : {}");
    }

    @Test
    public void testAddString() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.addString("{\"foo\" : {}}");
        JSONAssert.assertEquals("[{\"foo\" : {}}]", jsonBuilder.writeAsString(), true);
    }


    @Test(expected = JsonBuilderException.class)
    public void testAddStringWithBrokenJson() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.addString("{\"foo\" : {}");
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


    @Test(expected = JsonBuilderException.class)
    public void testJsonWithArray() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();

        JsonNode jsonNode = jsonBuilder.
                object("foo").
                root().
                getCurrent();

        jsonBuilder.array("bar").json(jsonNode);
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

    @Test(expected = JsonBuilderException.class)
    public void testAddJsonWithObject() throws Exception {
        JsonBuilder jsonBuilderTest = new JsonBuilder();

        JsonNode jsonNode = jsonBuilderTest.
                object("foo").
                root().
                getCurrent();

        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object("arr").addJson(jsonNode);
    }


    @Test
    public void testValue() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.value("foo", "bar");
        JSONAssert.assertEquals("{\"foo\":\"bar\"}", jsonBuilder.writeAsString(), true);
    }

    @Test
    public void testValueWithNullKey() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object().value(null, "bar");
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
    public void testPaths() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object("n1").object("n2").object("n3").object("n4");
        jsonBuilder.root();
        jsonBuilder.paths("n1/n2");
        jsonBuilder.object("n33");
        JSONAssert.assertEquals("{\"n1\":{\"n2\":{\"n3\":{\"n4\":{}},\"n33\":{}}}}", jsonBuilder.writeAsString(), true);
    }


    @Test(expected = JsonBuilderException.class)
    public void testPathsNotExists() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object("n1").object("n2").object("n3").object("n4");
        jsonBuilder.root();
        jsonBuilder.paths("n1/n2/n4");

    }

}
