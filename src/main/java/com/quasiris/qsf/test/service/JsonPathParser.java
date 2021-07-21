package com.quasiris.qsf.test.service;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

public class JsonPathParser {


    public static String getValue(String path, Object object) {
        try {
            String value = JsonPath.parse(object).read(path, String.class);
            return value;
        } catch (Exception e) {
            return null;
        }
    }
    public static String getValueFromContext(String path, DocumentContext documentContext) {
        try {
            String value = documentContext.read(path, String.class);
            return value;
        } catch (Exception e) {
            return null;
        }
    }

    public static DocumentContext getDocumentContext(String value) {
        return JsonPath.parse(value);
    }
}
