package com.quasiris.qsf.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.test.json.JsonAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class JsonBuilderTest {


    @Test
    public void testGetObjectNode() throws Exception {
        ObjectNode objectNode = JsonBuilder.create().
                string("{}").
                getObjectNode();
        JsonAssert.assertJson("{}", objectNode);
    }
    @Test
    public void testGetObjectNodeArray() throws Exception {
        ObjectNode objectNode = JsonBuilder.create().
                string("[]").
                getObjectNode();
        JsonAssert.assertJson("{\"values\" : []}", objectNode);
    }
    @Test
    public void testEmptyObject() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object();
        JsonAssert.assertJson("{}", jsonBuilder.get());
    }

    @Test
    public void testEmptyArray() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.array();
        JsonAssert.assertJson("[]", jsonBuilder.get());
    }

    @Test
    public void testArray() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object();
        jsonBuilder.array("foo");
        JsonAssert.assertJson("{\"foo\" : []}", jsonBuilder.get());
    }

    @Test
    public void testObject() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object();
        jsonBuilder.object("foo");
        JsonAssert.assertJson("{\"foo\" : {}}", jsonBuilder.get());
    }

    @Test
    public void testObjectWithoutWrapper() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object("foo");
        JsonAssert.assertJson("{\"foo\" : {}}", jsonBuilder.get());
    }

    @Test
    public void testObjectWithArray() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.array();
        jsonBuilder.object("foo");
        JsonAssert.assertJson("[{\"foo\" : {}}]", jsonBuilder.get());
    }


    @Test
    public void testArrayEmptyObject() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object();
        jsonBuilder.array("foo");
        jsonBuilder.object();
        JsonAssert.assertJson("{\"foo\":[{}]}", jsonBuilder.get());
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
        JsonAssert.assertJson("{\"foo\" : {}}", jsonBuilder.get());
    }



    @Test
    public void testReplaceValueInTextNode() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.string("{\"my-string-value\" : \"[jsonBuilder:replace] This is a text where i want to replace ${myVar}\"}");
        jsonBuilder.valueMap("myVar", "myValue");
        jsonBuilder.replace();
        JsonAssert.assertJson("{\"my-string-value\" : \"This is a text where i want to replace myValue\"}", jsonBuilder.get());
    }

    @Test
    public void testReplaceLong() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.string("{\"my-long-value\" : \"$myValue\"}");
        jsonBuilder.valueMap("myValue", 1L);
        jsonBuilder.replace();
        JsonAssert.assertJson("{\"my-long-value\" : 1}", jsonBuilder.get());
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
        JsonAssert.assertJson("{\"test\":{\"foo\":{}}}", jsonBuilder.get());
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
        JsonAssert.assertJson("[{\"foo\" : {}}]", jsonBuilder.get());
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
        JsonAssert.assertJson("{\"foo\" : \"bar\"}", jsonBuilder.get());
    }

    @Test
    public void testPojoWithKey() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        TestPojo testPojo = new TestPojo("bar");
        jsonBuilder.pojo("test", testPojo);
        JsonAssert.assertJson("{\"test\":{\"foo\":\"bar\"}}", jsonBuilder.get());
    }


    @Test
    public void testAddPojo() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.array();

        TestPojo testPojo = new TestPojo("bar");
        jsonBuilder.addPojo(testPojo);
        JsonAssert.assertJson("[{\"foo\" : \"bar\"}]", jsonBuilder.get());
    }

    @Test
    public void testPojoWithObject() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        TestPojo testPojo = new TestPojo("bar");
        jsonBuilder.object().pojo(testPojo);
        JsonAssert.assertJson("{\"foo\" : \"bar\"}", jsonBuilder.get());
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
        JsonAssert.assertJson("{\"foo\": { \"bar\": {}, \"test\": {} }}", jsonBuilder.get());
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
        JsonAssert.assertJson("{\"foo\": { \"bar\": {}, \"test\": {} }}", jsonBuilder.get());
    }

    @Test
    public void testRoot() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();

        jsonBuilder.
                object("foo").
                object("bar").
                root().
                object("test");

        JsonAssert.assertJson("{\"foo\": { \"bar\": {}}, \"test\" : {}}", jsonBuilder.get());
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
        JsonAssert.assertJson("{}", jsonNode);
    }

    @Test
    public void testGet() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();

        JsonNode jsonNode = jsonBuilder.
                object("foo").
                object("bar").
                get();
        JsonAssert.assertJson("{\"foo\":{\"bar\":{}}}", jsonNode);
    }

    @Test
    public void testJson() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();

        JsonNode jsonNode = jsonBuilder.
                object("foo").
                root().
                getCurrent();

        jsonBuilder.object("bar").json(jsonNode);

        JsonAssert.assertJson("{\"foo\":{},\"bar\":{\"foo\":{}}}", jsonBuilder.get());
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

        JsonAssert.assertJson("{\"arr\":[{\"foo\":{}}]}", jsonBuilder.get());
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
        JsonAssert.assertJson("{\"foo\":\"bar\"}", jsonBuilder.get());
    }

    @Test
    public void testValueWithNullKey() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object().object(null, "bar");
        JsonAssert.assertJson("{}", jsonBuilder.get());
    }

    @Test
    public void testAddValue() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.array().addValue("foo");
        JsonAssert.assertJson("[\"foo\"]", jsonBuilder.get());
    }

    @Test
    public void testPath() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object("n1").object("n2").object("n3").object("n4");
        jsonBuilder.root();
        jsonBuilder.path("n1").path("n2");
        jsonBuilder.object("n33");
        JsonAssert.assertJson("{\"n1\":{\"n2\":{\"n3\":{\"n4\":{}},\"n33\":{}}}}", jsonBuilder.get());
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
        JsonAssert.assertJson("{\"n1\":{\"n2\":{\"n3\":{\"n4\":{\"n33\":{}}}}}}", jsonBuilder.get());
    }


    @Test
    public void testPaths() throws Exception {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object("n1").object("n2").object("n3").object("n4");
        jsonBuilder.root();
        jsonBuilder.paths("n1/n2");
        jsonBuilder.object("n33");
        JsonAssert.assertJson("{\"n1\":{\"n2\":{\"n3\":{\"n4\":{}},\"n33\":{}}}}", jsonBuilder.get());
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
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.object("$foo", "bar");

        jsonBuilder.valueMap("foo", "bar");
        jsonBuilder.replace();

        JsonAssert.assertJson("{\"bar\": \"bar\"}", jsonBuilder.get());
    }

}
