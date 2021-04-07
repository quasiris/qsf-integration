package com.quasiris.qsf.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlUtilTest {

    @Test
    void removePassword() {
        String url = UrlUtil.removePassword("https://myUser:myPassword@elastic.quasiris.de:9200/product-search");
        assertEquals("https://elastic.quasiris.de:9200/product-search", url);
    }
    @Test
    void removePasswordFromUrlWithoutPassword() {
        String url = UrlUtil.removePassword("https://elastic.quasiris.de:9200/product-search");
        assertEquals("https://elastic.quasiris.de:9200/product-search", url);
    }
}