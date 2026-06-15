package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.quasiris.qsf.commons.util.ParameterUtils;
import com.quasiris.qsf.dto.query.HistogramFacetConfigDTO;
import com.quasiris.qsf.dto.query.MetricDTO;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.query.*;
import com.quasiris.qsf.util.QsfIntegrationConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AggregationMapper {

    @Deprecated // TODO remove this
    public static JsonNode createAgg(Facet facet, boolean isSubFacet, SearchQuery searchQuery) {
        return createAgg(facet, isSubFacet, null, null, searchQuery);
    }

    public static String mapType(String type) {
        if(type.equals("year")) {
            return "date_histogram";
        }

        if(type.equals("navigation")) {
            return "terms";
        }


        return type;
    }

    public static JsonNode createAgg(Facet facet, boolean isSubFacet, JsonNode filters, String variantId, SearchQuery searchQuery) {

        try {

            if("elastic".equals(facet.getType())) {
                JsonNode query = ParameterUtils.getParameter(facet.getParameters(), "query", null, JsonNode.class);
                return query;
            }

            String name = facet.getId();
            if(isSubFacet) {
                name = "subFacet";
            }

            JsonBuilder jsonBuilder = new JsonBuilder();
            jsonBuilder.
                    object(name).
                    object(mapType(facet.getType())).
                    object("field", facet.getFieldName()).
                    object("include", facet.getInclude()).
                    object("exclude", facet.getExclude());

            if("histogram".equals(facet.getType())) {
                // https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-histogram-aggregation.html
                // min_doc_count
                // interval
                // missing - not supported
                // extended_bounds - not supported

                Integer minDocCount = getValueOrDefault(facet.getParameters(), "min_doc_count", 1);
                Float interval = getValueOrDefault(facet.getParameters(), "interval", 1.0f);
                jsonBuilder.object("interval", interval);
                jsonBuilder.object("min_doc_count", minDocCount);

            } else if("date_histogram".equals(facet.getType())) {
                // https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-datehistogram-aggregation.html#fixed_intervals
                HistogramFacetConfigDTO histogramFacetConfigDTO = HistogramFacet.loadHistogramFacetConfigDTO(facet.getParameters());
                SearchFilter timestampFilter = searchQuery != null ? searchQuery.getSearchFilterById(facet.getId()) : null;
                JsonNode interval = HistogramFacet.getIntervalJson(timestampFilter, histogramFacetConfigDTO.getIntervals());
                String timeZone = histogramFacetConfigDTO.getTimeZone() != null ? histogramFacetConfigDTO.getTimeZone() : "Europe/Berlin";
                Integer minDocCount = histogramFacetConfigDTO.getMinDocCount() != null ? histogramFacetConfigDTO.getMinDocCount() : 0;
                if(histogramFacetConfigDTO.getQuery() != null) {
                    return JsonBuilder.create().json(histogramFacetConfigDTO.getQuery()).valueMap("interval", interval).replace().get();
                }
                jsonBuilder.json(interval).object("time_zone", timeZone).object("min_doc_count", minDocCount);
                if (histogramFacetConfigDTO.getMetrics() != null && !histogramFacetConfigDTO.getMetrics().isEmpty()) {
                    JsonNode metricsAggsNode = buildMetricsAggs(histogramFacetConfigDTO.getMetrics());
                    if (!metricsAggsNode.isEmpty()) {
                        jsonBuilder.
                                root().
                                path(name).
                                object("aggs").
                                json(metricsAggsNode);
                    }
                }
            } else if ("year".equals(facet.getType())) {
                jsonBuilder.
                        object("calendar_interval", "year").
                        object("time_zone", getValueOrDefault(facet.getParameters(), "time_zone", "Europe/Berlin")).
                        object("format", "yyyy").
                        object("min_doc_count", 0);
            } else if ("cardinality".equals(facet.getType())) {
            } else {
                jsonBuilder.
                        object("size", facet.getSize());

            }

            if(facet.getSortBy() != null) {
                jsonBuilder.
                        stash().
                        object("order").
                        object(facet.getSortBy(), facet.getSortOrder()).
                        unstash();
            }


            if(facet.getChildren() != null) {
                jsonBuilder.root().path(name);
                // Metrics aggs (percentiles, avg, etc.) are not bucket aggs — use their own ID, not "subFacet"
                // Metrics aggs also cannot have sub-aggs, so never forward variantId into them
                boolean childIsMetrics = isMetricsAggType(facet.getChildren().getType());
                String childVariantId = childIsMetrics ? null : variantId;
                JsonNode subAggs = createAgg(facet.getChildren(), !childIsMetrics, null, childVariantId, searchQuery);
                jsonBuilder.json("aggs", subAggs);
            }

            if(filters != null) {
                JsonBuilder aggFilterWrapper = JsonBuilder.create().
                        object(name + "_filter_wrapper").
                        stash().
                        object("filter").
                        json(filters).
                        unstash().
                        json("aggs", jsonBuilder.get());

                jsonBuilder = aggFilterWrapper;
            }

            if(StringUtils.isNotEmpty(variantId) && !isSubFacet) {
                String facetPath = name;
                if(filters != null) {
                    facetPath = name+"_filter_wrapper/aggs/"+name;
                }

                if(!isSubFacet) {
                    facetPath = facetPath + "/aggs";
                }
                JsonBuilder jsonBuilderVariant = new JsonBuilder();
                jsonBuilderVariant
                        .object(QsfIntegrationConstants.VARIANT_COUNT_SUB_AGGREGATION_NAME)
                        .object("cardinality")
                        .object("field", variantId+".keyword");

                jsonBuilder = JsonBuilder.create()
                        .newJson(jsonBuilder.replace().get())
                        .pathsForceCreate(facetPath)
                        .json(jsonBuilderVariant.get());
            }

            return jsonBuilder.get();

        } catch (JsonBuilderException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isMetricsAggType(String type) {
        return "percentiles".equals(type) || "avg".equals(type) || "sum".equals(type)
                || "min".equals(type) || "max".equals(type) || "cardinality".equals(type);
    }

    private static JsonNode buildMetricsAggs(List<MetricDTO> metrics) throws JsonBuilderException {
        JsonBuilder aggsBuilder = new JsonBuilder().object();
        for (MetricDTO metric : metrics) {
            if (metric.getType() == null || "count".equals(metric.getType())) {
                continue;
            }
            String id = metric.getId() != null ? metric.getId() : metric.getType();
            JsonBuilder metricBuilder = new JsonBuilder()
                    .object(id)
                    .object(metric.getType())
                    .object("field", metric.getFieldName());
            if ("percentiles".equals(metric.getType())) {
                metricBuilder.object("percents", metric.getPercents());
            }
            aggsBuilder.json(metricBuilder.get());
        }
        return aggsBuilder.get();
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValueOrDefault(Map<String, Object> parameters, String key, T defaultValue) {
        if (parameters != null && parameters.containsKey(key)) {
            Object value = parameters.get(key);

            if (defaultValue != null) {
                Class<?> defaultClass = defaultValue.getClass();

                // Direct type check
                if (defaultClass.isInstance(value)) {
                    return (T) value;
                }

                // Type conversion logic
                if (value instanceof Number) {
                    Number numberValue = (Number) value;
                    if (defaultValue instanceof Float) {
                        return (T) Float.valueOf(numberValue.floatValue());
                    } else if (defaultValue instanceof Double) {
                        return (T) Double.valueOf(numberValue.doubleValue());
                    } else if (defaultValue instanceof Integer) {
                        return (T) Integer.valueOf(numberValue.intValue());
                    } else if (defaultValue instanceof Long) {
                        return (T) Long.valueOf(numberValue.longValue());
                    } else if (defaultValue instanceof Short) {
                        return (T) Short.valueOf(numberValue.shortValue());
                    } else if (defaultValue instanceof Byte) {
                        return (T) Byte.valueOf(numberValue.byteValue());
                    }
                }
            }
        }
        return defaultValue;
    }


    public static JsonNode createSlider(Facet slider) {
        return createSlider(slider, null);
    }

    public static JsonNode createSlider(Facet slider, JsonNode filters) {

        try {
            JsonBuilder jsonBuilder = new JsonBuilder();
            jsonBuilder.
                    object(slider.getId()).
                    object("stats").
                    object("field", slider.getFieldName());

            if(filters != null) {
                JsonBuilder aggFilterWrapper = JsonBuilder.create().
                        object(slider.getId() + "_filter_wrapper").
                        stash().
                        object("filter").
                        json(filters).
                        unstash().
                        json("aggs", jsonBuilder.get());

                jsonBuilder = aggFilterWrapper;
            }

            return jsonBuilder.get();
        } catch (JsonBuilderException e) {
            throw new RuntimeException(e);
        }
    }


    public static JsonNode createRangeFacet(RangeFacet rangeFacet, JsonNode filters) {

        try {
            JsonBuilder jsonBuilder = new JsonBuilder();
            jsonBuilder.
                    object(rangeFacet.getId()).
                    object("range").
                    object("keyed", Boolean.FALSE).
                    object("field", rangeFacet.getFieldName());

            jsonBuilder.array("ranges");
            for(Range range : rangeFacet.getRanges()) {
                jsonBuilder.stash("loop");
                jsonBuilder.object();
                jsonBuilder.stash("range");
                if(range.getMin() != null) {
                    jsonBuilder.object("from", range.getMin());
                }
                jsonBuilder.unstash("range");

                jsonBuilder.stash("range");
                jsonBuilder.object("key", range.getValue());
                jsonBuilder.unstash("range");

                if(range.getMax() != null) {
                    jsonBuilder.object("to", range.getMax());
                }

                jsonBuilder.unstash("loop");

            }

            if(filters != null) {
                JsonBuilder aggFilterWrapper = JsonBuilder.create().
                        object(rangeFacet.getId() + "_filter_wrapper").
                        stash().
                        object("filter").
                        json(filters).
                        unstash().
                        json("aggs", jsonBuilder.get());

                jsonBuilder = aggFilterWrapper;
            }

            return jsonBuilder.get();
        } catch (JsonBuilderException e) {
            throw new RuntimeException(e);
        }
    }
}
