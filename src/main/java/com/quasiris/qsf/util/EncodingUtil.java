package com.quasiris.qsf.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by mki on 18.11.17.
 */

// use UrlUtil.encode
@Deprecated
public class EncodingUtil {

    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
}
