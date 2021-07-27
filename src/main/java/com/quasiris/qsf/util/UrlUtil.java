package com.quasiris.qsf.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class UrlUtil {

    public static String removePassword(String url) {
        return url.replaceAll("(.*)://(.*)@(.*)", "$1://$3");
    }

    public static Map<String, Object> encode(Map<String, Object> values, String suffix) {
        Map<String, Object> ret = new HashMap<>();

        for(Map.Entry<String, Object> entry : values.entrySet()) {
            Object encoded = entry.getValue();
            if(entry.getValue() instanceof String) {
                encoded = encode((String) entry.getValue());
            }
            if(suffix != null) {
                ret.put(entry.getKey(), entry.getValue());
                ret.put(entry.getKey() + suffix, encoded);
            } else {
                ret.put(entry.getKey(), encoded);
            }

        }
        return ret;
    }

    public static Map<String, Object> encode(Map<String, Object> values) {
        return encode(values, null);
    }

    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
}
