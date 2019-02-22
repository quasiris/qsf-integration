package com.quasiris.qsf.query;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class PosTagTest {


    @Test
    public void isOneOfStringValue() {
        Assert.assertTrue(PosTag.isOneOfValue("<ART>", PosTag.SYM, PosTag.BRAND, PosTag.ART));
        Assert.assertFalse(PosTag.isOneOfValue("<ART>", PosTag.SYM, PosTag.BRAND));
    }

    @Test
    public void isOneOfValue() {
        Assert.assertTrue(PosTag.isOneOfValue(PosTag.ART, PosTag.SYM, PosTag.BRAND, PosTag.ART));
        Assert.assertFalse(PosTag.isOneOfValue(PosTag.ART, PosTag.SYM, PosTag.BRAND));
    }
}