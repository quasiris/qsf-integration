package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.query.Facet;
import com.quasiris.qsf.query.Slider;

public class AggregationMapper {

    public static JsonNode createAgg(Facet facet, boolean isSubFacet) {
        return createAgg(facet, isSubFacet, null);
    }

    public static String mapType(String type) {
        if(type.equals("year")) {
            return "date_histogram";
        }

        return type;
    }

    public static JsonNode createAgg(Facet facet, boolean isSubFacet, JsonNode filters) {

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
                jsonBuilder.
                        object("calendar_interval", "hour").
                        object("time_zone", "Europe/Berlin").
                        object("min_doc_count", 0);
            } else if ("year".equals(facet.getType())) {
                jsonBuilder.
                        object("calendar_interval", "year").
                        object("time_zone", "Europe/Berlin").
                        object("format", "yyyy").
                        object("min_doc_count", 0);
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
                JsonNode subAggs = createAgg(facet.getChildren(), true);
                jsonBuilder.json("aggs", subAggs);
            }

            if(filters != null) {
                JsonBuilder aggFilterWrapper = JsonBuilder.create().
                        object(name + "_filter_wrapper").
                        stash().
                        object("filter").
                        json("bool", filters).
                        unstash().
                        json("aggs", jsonBuilder.get());

                jsonBuilder = aggFilterWrapper;
            }

            return jsonBuilder.get();

        } catch (JsonBuilderException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode createSlider(Slider slider) {
        return createSlider(slider, null);
    }

    public static JsonNode createSlider(Slider slider, JsonNode filters) {

        try {
            JsonBuilder jsonBuilder = new JsonBuilder();
            jsonBuilder.
                    object(slider.getName()).
                    object(slider.getType()).
                    object("field", slider.getId());

            if(filters != null) {
                JsonBuilder aggFilterWrapper = JsonBuilder.create().
                        object(slider.getName() + "_filter_wrapper").
                        stash().
                        object("filter").
                        json("bool", filters).
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
