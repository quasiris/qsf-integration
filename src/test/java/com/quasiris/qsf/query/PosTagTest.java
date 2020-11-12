package com.quasiris.qsf.query;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PosTagTest {


    @Test
    public void isOneOfStringValue() {
        assertTrue(PosTag.isOneOfValue("<ART>", PosTag.SYM, PosTag.BRAND, PosTag.ART));
        assertFalse(PosTag.isOneOfValue("<ART>", PosTag.SYM, PosTag.BRAND));
    }

    @Test
    public void isOneOfValue() {
        assertTrue(PosTag.isOneOfValue(PosTag.ART, PosTag.SYM, PosTag.BRAND, PosTag.ART));
        assertFalse(PosTag.isOneOfValue(PosTag.ART, PosTag.SYM, PosTag.BRAND));
    }
}