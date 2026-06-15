package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.dto.query.HistogramFacetConfigDTO;
import com.quasiris.qsf.dto.query.IntervalDTO;
import com.quasiris.qsf.dto.query.MetricDTO;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.query.Facet;
import com.quasiris.qsf.query.FilterOperator;
import com.quasiris.qsf.test.TestUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AggregationMapperTest {
    @Test
    void createAgg() throws JsonBuilderException, IOException {
        // given
        Facet facet = new Facet();
        facet.setId("attr_farbe");
        facet.setFieldName("attr_farbe.keyword");
        facet.setName("attr_farbe");
        facet.setType("terms");
        facet.setSize(5);
        facet.setSortOrder("asc");
        facet.setOperator(FilterOperator.OR);

        boolean isSubFacet = false;
        JsonNode filters = null;
        String variantId = null;

        // when
        JsonNode agg = AggregationMapper.createAgg(facet, isSubFacet, filters, variantId, null);

        // then
        ObjectNode queryNode = TestUtils.mockQuery("/com/quasiris/qsf/pipeline/filter/elastic/query/single-facet.json");
        assertEquals(queryNode.toPrettyString(), agg.toPrettyString());
    }

    @Test
    void createAggForVariant() throws JsonBuilderException, IOException {
        // given
        Facet facet = new Facet();
        facet.setId("attr_farbe");
        facet.setFieldName("attr_farbe.keyword");
        facet.setName("attr_farbe");
        facet.setType("terms");
        facet.setSize(5);
        facet.setSortOrder("asc");
        facet.setOperator(FilterOperator.OR);

        boolean isSubFacet = false;
        JsonNode filters = null;
        String variantId = "variantId";

        // when
        JsonNode agg = AggregationMapper.createAgg(facet, isSubFacet, filters, variantId, null);

        // then
        ObjectNode queryNode = TestUtils.mockQuery("/com/quasiris/qsf/pipeline/filter/elastic/query/single-facet-with-variant.json");
        assertEquals(queryNode.toPrettyString(), agg.toPrettyString());
    }

    @Test
    void createDateHistogramWithPercentiles() throws JsonBuilderException, IOException {
        // given — date_histogram configured via HistogramFacetConfigDTO with metrics
        Facet facet = new Facet();
        facet.setId("duration_over_time");
        facet.setFieldName("timestamp");
        facet.setType("date_histogram");

        IntervalDTO intervalDTO = new IntervalDTO();
        intervalDTO.setType("calendar_interval");
        intervalDTO.setInterval("month");

        MetricDTO countMetric = new MetricDTO();
        countMetric.setType("count");

        MetricDTO percentilesMetric = new MetricDTO();
        percentilesMetric.setType("percentiles");
        percentilesMetric.setId("duration_percentiles");
        percentilesMetric.setFieldName("duration");
        percentilesMetric.setPercents(Arrays.asList(50, 90, 95, 99));

        HistogramFacetConfigDTO config = new HistogramFacetConfigDTO();
        config.setIntervals(Arrays.asList(intervalDTO));
        config.setMetrics(Arrays.asList(countMetric, percentilesMetric));
        facet.addParameter("config", config);

        // when
        JsonNode agg = AggregationMapper.createAgg(facet, false, null, null, null);

        // then
        ObjectNode expected = TestUtils.mockQuery("/com/quasiris/qsf/pipeline/filter/elastic/query/date-histogram-with-percentiles.json");
        assertEquals(expected.toPrettyString(), agg.toPrettyString());
    }

    @Test
    void createDateHistogramWithMultipleMetrics() throws JsonBuilderException, IOException {
        // given
        Facet facet = new Facet();
        facet.setId("duration_over_time");
        facet.setFieldName("timestamp");
        facet.setType("date_histogram");

        IntervalDTO intervalDTO = new IntervalDTO();
        intervalDTO.setType("calendar_interval");
        intervalDTO.setInterval("month");

        MetricDTO countMetric = new MetricDTO();
        countMetric.setType("count");

        MetricDTO durationPercentiles = new MetricDTO();
        durationPercentiles.setType("percentiles");
        durationPercentiles.setId("duration_percentiles");
        durationPercentiles.setFieldName("duration");
        durationPercentiles.setPercents(Arrays.asList(50, 90, 95, 99));

        MetricDTO sizePercentiles = new MetricDTO();
        sizePercentiles.setType("percentiles");
        sizePercentiles.setId("size_percentiles");
        sizePercentiles.setFieldName("size");
        sizePercentiles.setPercents(Arrays.asList(1, 50, 90, 95, 99));

        HistogramFacetConfigDTO config = new HistogramFacetConfigDTO();
        config.setIntervals(Arrays.asList(intervalDTO));
        config.setMetrics(Arrays.asList(countMetric, durationPercentiles, sizePercentiles));
        facet.addParameter("config", config);

        // when
        JsonNode agg = AggregationMapper.createAgg(facet, false, null, null, null);

        // then
        ObjectNode expected = TestUtils.mockQuery("/com/quasiris/qsf/pipeline/filter/elastic/query/date-histogram-with-multiple-metrics.json");
        assertEquals(expected.toPrettyString(), agg.toPrettyString());
    }

    @Test
    void createAggForVariantWithFilter() throws JsonBuilderException, IOException {
        // given
        Facet facet = new Facet();
        facet.setId("attr_farbe");
        facet.setFieldName("attr_farbe.keyword");
        facet.setName("attr_farbe");
        facet.setType("terms");
        facet.setSize(5);
        facet.setSortOrder("asc");
        facet.setOperator(FilterOperator.OR);

        boolean isSubFacet = false;
        JsonNode filters = JsonBuilder.create().object("test", "test").get();
        String variantId = "variantId";

        // when
        JsonNode agg = AggregationMapper.createAgg(facet, isSubFacet, filters, variantId, null);

        // then
        ObjectNode queryNode = TestUtils.mockQuery("/com/quasiris/qsf/pipeline/filter/elastic/query/single-facet-with-variant-and-filter.json");
        assertEquals(queryNode.toPrettyString(), agg.toPrettyString());
    }
}