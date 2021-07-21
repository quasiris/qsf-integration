package com.quasiris.qsf.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class UrlUtil {

    public static String removePassword(String url) {
        return url.replaceAll("(.*)://(.*)@(.*)", "$1://$3");
    }

    public static String replaceQueryParameter(String url, String param, String value) {
        // TODO implement
        return url;

    }

    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
}
