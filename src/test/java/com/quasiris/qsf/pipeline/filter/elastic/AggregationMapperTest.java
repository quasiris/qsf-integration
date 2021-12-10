package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.query.Facet;
import com.quasiris.qsf.query.FilterOperator;
import com.quasiris.qsf.test.TestUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AggregationMapperTest {
    @Test
    void createAgg() throws JsonBuilderException, IOException {
        // given
        Facet facet = new Facet();
        facet.setId("attr_farbe.keyword");
        facet.setName("attr_farbe");
        facet.setType("terms");
        facet.setSize(5);
        facet.setSortOrder("asc");
        facet.setOperator(FilterOperator.OR);

        boolean isSubFacet = false;
        JsonNode filters = null;
        String variantId = null;

        // when
        JsonNode agg = AggregationMapper.createAgg(facet, isSubFacet, filters, variantId);

        // then
        ObjectNode queryNode = TestUtils.mockQuery("/com/quasiris/qsf/pipeline/filter/elastic/query/single-facet.json");
        assertEquals(queryNode.toPrettyString(), agg.toPrettyString());
    }

    @Test
    void createAggForVariant() throws JsonBuilderException, IOException {
        // given
        Facet facet = new Facet();
        facet.setId("attr_farbe.keyword");
        facet.setName("attr_farbe");
        facet.setType("terms");
        facet.setSize(5);
        facet.setSortOrder("asc");
        facet.setOperator(FilterOperator.OR);

        boolean isSubFacet = false;
        JsonNode filters = null;
        String variantId = "variantId";

        // when
        JsonNode agg = AggregationMapper.createAgg(facet, isSubFacet, filters, variantId);

        // then
        ObjectNode queryNode = TestUtils.mockQuery("/com/quasiris/qsf/pipeline/filter/elastic/query/single-facet-with-variant.json");
        assertEquals(queryNode.toPrettyString(), agg.toPrettyString());
    }

    @Test
    void createAggForVariantWithFilter() throws JsonBuilderException, IOException {
        // given
        Facet facet = new Facet();
        facet.setId("attr_farbe.keyword");
        facet.setName("attr_farbe");
        facet.setType("terms");
        facet.setSize(5);
        facet.setSortOrder("asc");
        facet.setOperator(FilterOperator.OR);

        boolean isSubFacet = false;
        JsonNode filters = JsonBuilder.create().object("test", "test").get();
        String variantId = "variantId";

        // when
        JsonNode agg = AggregationMapper.createAgg(facet, isSubFacet, filters, variantId);

        // then
        ObjectNode queryNode = TestUtils.mockQuery("/com/quasiris/qsf/pipeline/filter/elastic/query/single-facet-with-variant-and-filter.json");
        assertEquals(queryNode.toPrettyString(), agg.toPrettyString());
    }
}