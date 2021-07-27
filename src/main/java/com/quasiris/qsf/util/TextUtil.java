package com.quasiris.qsf.util;

import org.apache.commons.text.StringSubstitutor;

import java.util.Map;

public class TextUtil {

    public static String replace(String value, Map<String, Object> valueMap) {
        StringSubstitutor stringSubstitutor = new StringSubstitutor(valueMap);
        return stringSubstitutor.replace(value);
    }
}
