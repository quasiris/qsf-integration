package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.quasiris.qsf.commons.text.date.SupportedDateFormatsParser;
import com.quasiris.qsf.commons.util.JsonUtil;
import com.quasiris.qsf.dto.query.HistogramFacetConfigDTO;
import com.quasiris.qsf.dto.query.IntervalDTO;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.query.*;
import com.quasiris.qsf.util.QsfIntegrationConstants;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.Instant;
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
                JsonNode query = getParameter(facet.getParameters(), "query", null, JsonNode.class);
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

            if("date_histogram".equals(facet.getType())) {
                // https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-datehistogram-aggregation.html#fixed_intervals
                HistogramFacetConfigDTO histogramFacetConfigDTO = getParameter(facet.getParameters(), "config", null, HistogramFacetConfigDTO.class);
                if(histogramFacetConfigDTO == null) {
                    histogramFacetConfigDTO = JsonBuilder.create().
                            classpath("com/quasiris/qsf/elastic/config/default-histogram-facet-config.json").
                            get(HistogramFacetConfigDTO.class);
                }

                SearchFilter timestampFilter = searchQuery.getSearchFilterById(facet.getId());
                JsonNode interval = getInterval(timestampFilter, histogramFacetConfigDTO.getIntervals());
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

    static JsonNode getInterval(SearchFilter timestampFilter, List<IntervalDTO> intervalConfigList) throws JsonBuilderException {

        IntervalDTO interval = null;
        if(timestampFilter == null) {
            // take the last one in the config
            interval = intervalConfigList.get(intervalConfigList.size() - 1);
        } else {
            String from = timestampFilter.getMinValue().toString();
            String to = timestampFilter.getMaxValue().toString();
            long minutesBetween = getMinutesBetween(from, to);
            for(IntervalDTO intervalItem : intervalConfigList) {
                if(intervalItem.getMinute() == null || minutesBetween < intervalItem.getMinute()) {
                    interval = intervalItem;
                    break;
                }
            }
        }



        if(interval == null) {
            return JsonBuilder.create().object("calendar_interval", "hour").get();
        }
        return JsonBuilder.create().object(interval.getType(), interval.getInterval()).get() ;
    }

    static long getMinutesBetween(String from, String to) {
        Instant fromInstant = SupportedDateFormatsParser.requireInstantFromString(from);
        Instant toInstant = SupportedDateFormatsParser.requireInstantFromString(to);
        long minutesBetween  = Duration.between(fromInstant, toInstant).toMinutes();
        return minutesBetween;
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

    static <T> T getParameter(Map<String, Object> parameters, String param, T defaultValue, Class<T> toValueType) {
        if(parameters == null) {
            return defaultValue;
        }
        Object value = parameters.get(param);
        if(value == null) {
            return defaultValue;
        }

        return JsonUtil.defaultMapper().convertValue(value, toValueType);
    }
}
