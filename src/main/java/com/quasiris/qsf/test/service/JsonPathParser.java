package com.quasiris.qsf.test.service;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;

public class JsonPathParser {


    /**
     * returns the value at the given Json path in the given object
     * @param path String, a jayway Json path <br>
     *             see <a href="https://github.com/json-path/JsonPath#operators">github.com/json-path/JsonPath</a> for a set of operators and examples
     * @param object doc
     * @return doc
     */
    public static String getValue(String path, Object object) {
        try {
            Object value = JsonPath.parse(object).read(path, Object.class);
            return getStringValue(value);
        } catch (Exception e) {
            return null;
        }
    }
    public static String getValueFromContext(String path, DocumentContext documentContext) {
        try {
            Object value = documentContext.read(path, Object.class);
            return getStringValue(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getStringValue(Object value) {
        if(value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            return array.get(0).toString();
        }
        return value.toString();
    }

    public static DocumentContext getDocumentContext(String value) {
        return JsonPath.parse(value);
    }
}
