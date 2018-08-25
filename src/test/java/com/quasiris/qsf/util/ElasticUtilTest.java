package com.quasiris.qsf.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by oelbaer on 10.07.18.
 */
public class ElasticUtilTest {
    @Test
    public void escape() throws Exception {
        String value = ElasticUtil.escape("foo && bar");
        Assert.assertEquals("foo \\&& bar", value);

    }

}