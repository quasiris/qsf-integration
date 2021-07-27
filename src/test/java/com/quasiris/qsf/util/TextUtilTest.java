package com.quasiris.qsf.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TextUtilTest {


    @Test
    public void testReplaceString() {
        String url = "https://${hostname}/";
        Map<String, Object> paremters = new HashMap<>();
        paremters.put("hostname", "www.quasiris.de");
        String actual = TextUtil.replace(url, paremters);
        assertEquals("https://www.quasiris.de/", actual);
    }

    @Test
    public void testReplaceLong() {
        String number = "${number}";
        Map<String, Object> paremters = new HashMap<>();
        paremters.put("number", 42L);
        String actual = TextUtil.replace(number, paremters);
        assertEquals("42", actual);
    }

}