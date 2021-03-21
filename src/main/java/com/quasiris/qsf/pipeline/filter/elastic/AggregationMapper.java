package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.query.Facet;
import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.Slider;

import java.util.List;

public class AggregationMapper {

    public static JsonNode createAgg(Facet facet, boolean isSubFacet) {
        return createAgg(facet, isSubFacet, null);
    }

    public static JsonNode createAgg(Facet facet, boolean isSubFacet, List<SearchFilter> searchFilterList) {

        try {

            String name = facet.getName();
            if(isSubFacet) {
                name = "subFacet";
            }

            JsonBuilder jsonBuilder = new JsonBuilder();
            jsonBuilder.
                    object(name).
                    object(facet.getType()).
                    object("field", facet.getId()).
                    object("include", facet.getInclude()).
                    object("exclude", facet.getExclude()).
                    object("size", facet.getSize());

            if("date_histogram".equals(facet.getType())) {
                jsonBuilder.
                        object("calendar_interval", "hour").
                        object("time_zone", "Europe/Berlin").
                        object("min_doc_count", 0);
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

            if(searchFilterList != null) {

            }

            return jsonBuilder.get();

        } catch (JsonBuilderException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode createSlider(Slider slider) {

        try {
            JsonBuilder jsonBuilder = new JsonBuilder();
            jsonBuilder.
                    object(slider.getName()).
                    object(slider.getType()).
                    object("field", slider.getId());

            return jsonBuilder.get();
        } catch (JsonBuilderException e) {
            throw new RuntimeException(e);
        }
    }
}
