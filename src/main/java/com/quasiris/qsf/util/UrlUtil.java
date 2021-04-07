package com.quasiris.qsf.util;

public class UrlUtil {

    public static String removePassword(String url) {
        return url.replaceAll("(.*)://(.*)@(.*)", "$1://$3");
    }
}
