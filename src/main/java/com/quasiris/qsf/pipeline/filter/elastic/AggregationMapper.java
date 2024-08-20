package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.quasiris.qsf.dto.query.HistogramFacetConfigDTO;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.query.*;
import com.quasiris.qsf.util.QsfIntegrationConstants;
import org.apache.commons.lang3.StringUtils;

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
                JsonNode query = HistogramFacet.getParameter(facet.getParameters(), "query", null, JsonNode.class);
                return query;
            }

            String name = facet.getName();
            if(isSubFacet) {
                name = "subFacet";
            }

            JsonBuilder jsonBuilder = new JsonBuilder();
            jsonBuilder.
                    object(name).
                    object(mapType(facet.getType())).
                    object("field", facet.getId()).
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
                SearchFilter timestampFilter = searchQuery.getSearchFilterById(facet.getId());
                JsonNode interval = HistogramFacet.getIntervalJson(timestampFilter, histogramFacetConfigDTO.getIntervals());
                String timeZone = histogramFacetConfigDTO.getTimeZone();
                if(timeZone == null) {
                    timeZone = "Europe/Berlin";
                }
                Integer minDocCount = histogramFacetConfigDTO.getMinDocCount();
                if(minDocCount == null) {
                    minDocCount = 0;
                }
                if(histogramFacetConfigDTO.getQuery() != null) {
                    return JsonBuilder.create().json(histogramFacetConfigDTO.getQuery()).valueMap("interval", interval).replace().get();
                } else {
                    // deprecated ... used in the old qsc dashboard
                    jsonBuilder.json(interval);
                    jsonBuilder.
                            object("time_zone", getValueOrDefault(facet.getParameters(), "time_zone", timeZone)).
                            object("min_doc_count", minDocCount);
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
                JsonNode subAggs = createAgg(facet.getChildren(), true, null, variantId, searchQuery);
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
                    object(slider.getName()).
                    object("stats").
                    object("field", slider.getId());

            if(filters != null) {
                JsonBuilder aggFilterWrapper = JsonBuilder.create().
                        object(slider.getName() + "_filter_wrapper").
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
                    object(rangeFacet.getName()).
                    object("range").
                    object("keyed", Boolean.FALSE).
                    object("field", rangeFacet.getId());

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
                        object(rangeFacet.getName() + "_filter_wrapper").
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
