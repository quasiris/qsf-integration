package com.quasiris.qsf.util;

import org.apache.commons.text.StringSubstitutor;

import java.util.Map;

@Deprecated // move to qsf-commons
public class TextUtil {

    public static String replace(String value, Map<String, Object> valueMap) {
        StringSubstitutor stringSubstitutor = new StringSubstitutor(valueMap);
        return stringSubstitutor.replace(value);
    }
}
