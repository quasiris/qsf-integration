package com.quasiris.qsf.pipeline.filter.mapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateFormatFacetKeyMapperTest {


    @Test
    public void test2016() {

        DateFormatFacetKeyMapper mapper = new DateFormatFacetKeyMapper("yyyy");
        String actual = mapper.map("1451602800000");
        assertEquals("2016", actual);
    }

}