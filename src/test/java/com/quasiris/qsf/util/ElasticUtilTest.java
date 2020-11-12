package com.quasiris.qsf.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by oelbaer on 10.07.18.
 */
public class ElasticUtilTest {

    @Test
    public void escapeCORONA() throws Exception {
        String value = ElasticUtil.escape("CORONA");
        assertEquals("CORONA", value);
    }


    @Test
    public void escapeCARD() throws Exception {
        String value = ElasticUtil.escape("CANDIDATE");
        assertEquals("CANDIDATE", value);
    }


    @Test
    public void escapeSingleAnd() throws Exception {
        String value = ElasticUtil.escape("foo & bar");
        assertEquals("foo & bar", value);
    }

    @Test
    public void escape() throws Exception {
        String value = ElasticUtil.escape("foo && bar");
        assertEquals("foo \\&& bar", value);
    }

    @Test
    public void escapeORAtTheEnd() throws Exception {
        String value = ElasticUtil.escape("16515 OR");
        assertEquals("16515 OR", value);
    }

    @Test
    public void escapeOR() throws Exception {
        String value = ElasticUtil.escape("16515 OR 0815");
        assertEquals("16515 \\OR 0815", value);
    }

}