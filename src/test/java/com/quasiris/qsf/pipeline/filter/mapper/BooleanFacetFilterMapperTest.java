package com.quasiris.qsf.pipeline.filter.mapper;

import com.quasiris.qsf.dto.response.FacetValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BooleanFacetFilterMapperTest {

    private BooleanFacetFilterMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BooleanFacetFilterMapper();
        mapper.setFilterPrefix("f.");
        mapper.setFacetId("available");
    }

    @Test
    void mapOneToTrue() {
        mapper.setKey("1");
        FacetValue facetValue = new FacetValue("true", 1L);
        mapper.map(facetValue);
        assertEquals("f.available=true", facetValue.getFilter());
    }

    @Test
    void mapZeroToFalse() {
        mapper.setKey("0");
        FacetValue facetValue = new FacetValue("false", 1L);
        mapper.map(facetValue);
        assertEquals("f.available=false", facetValue.getFilter());
    }

    @Test
    void mapIntegerOneToTrue() {
        mapper.setKey(1);
        FacetValue facetValue = new FacetValue("true", 1L);
        mapper.map(facetValue);
        assertEquals("f.available=true", facetValue.getFilter());
    }

    @Test
    void mapIntegerZeroToFalse() {
        mapper.setKey(0);
        FacetValue facetValue = new FacetValue("false", 1L);
        mapper.map(facetValue);
        assertEquals("f.available=false", facetValue.getFilter());
    }
}
