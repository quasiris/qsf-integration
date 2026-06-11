package com.quasiris.qsf.pipeline.filter.mapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BooleanFacetKeyMapperTest {

    private final BooleanFacetKeyMapper mapper = new BooleanFacetKeyMapper();

    @Test
    void mapOne() {
        assertEquals("true", mapper.map("1"));
    }

    @Test
    void mapZero() {
        assertEquals("false", mapper.map("0"));
    }

    @Test
    void mapIntegerOne() {
        assertEquals("true", mapper.map(1));
    }

    @Test
    void mapIntegerZero() {
        assertEquals("false", mapper.map(0));
    }

    @Test
    void mapNull() {
        assertNull(mapper.map(null));
    }

    @Test
    void mapOtherValue() {
        assertEquals("maybe", mapper.map("maybe"));
    }
}
