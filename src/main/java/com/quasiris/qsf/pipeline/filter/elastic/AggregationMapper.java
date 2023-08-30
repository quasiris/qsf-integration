package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.query.Facet;
import com.quasiris.qsf.query.Range;
import com.quasiris.qsf.query.RangeFacet;
import com.quasiris.qsf.util.QsfIntegrationConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class AggregationMapper {
    @Deprecated // TODO remove this
    public static JsonNode createAgg(Facet facet, boolean isSubFacet) {
        return createAgg(facet, isSubFacet, null, null);
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

    public static JsonNode createAgg(Facet facet, boolean isSubFacet, JsonNode filters, String variantId) {

        try {

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

            if("date_histogram".equals(facet.getType())) {
                // https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-datehistogram-aggregation.html#fixed_intervals
                String fixedInterval = getValueOrDefault(facet.getParameters(), "fixed_interval", null);
                String calendarInterval = getValueOrDefault(facet.getParameters(), "calendar_interval", null);

                if(fixedInterval != null) {
                    jsonBuilder.object("fixed_interval", fixedInterval);
                } else if (calendarInterval != null) {
                    jsonBuilder.object("calendar_interval", calendarInterval);
                } else {
                    jsonBuilder.object("fixed_interval", "hour");
                }

                jsonBuilder.
                        object("time_zone", getValueOrDefault(facet.getParameters(), "time_zone", "Europe/Berlin")).
                        object("min_doc_count", 0);
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
                JsonNode subAggs = createAgg(facet.getChildren(), true, null, variantId);
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

            if(StringUtils.isNotEmpty(variantId)) {
                String facetPath = name;
                if(filters != null) {
                    facetPath = name+"_filter_wrapper/aggs/"+name;
                }
                JsonBuilder jsonBuilderVariant = new JsonBuilder();
                jsonBuilderVariant
                        .object(QsfIntegrationConstants.VARIANT_COUNT_SUB_AGGREGATION_NAME)
                        .object("cardinality")
                        .object("field", variantId+".keyword");

                jsonBuilder = JsonBuilder.create()
                        .newJson(jsonBuilder.replace().get())
                        .pathsForceCreate(facetPath)
                        .json("aggs", jsonBuilderVariant.get());
            }

            return jsonBuilder.get();

        } catch (JsonBuilderException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getValueOrDefault(Map<String, Object> parameters, String key, String defaultValue) {
        if(parameters == null) {
            return defaultValue;
        }

        Object value = parameters.get(key);
        if(value == null) {
            return defaultValue;
        }
        return value.toString();
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
