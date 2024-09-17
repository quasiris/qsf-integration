package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.quasiris.qsf.commons.text.date.SupportedDateFormatsParser;
import com.quasiris.qsf.commons.util.JsonUtil;
import com.quasiris.qsf.commons.util.ParameterUtils;
import com.quasiris.qsf.dto.query.HistogramFacetConfigDTO;
import com.quasiris.qsf.dto.query.IntervalDTO;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.json.JsonBuilderException;
import com.quasiris.qsf.query.SearchFilter;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class HistogramFacet {


    public static HistogramFacetConfigDTO loadHistogramFacetConfigDTO(Map<String, Object> parameters) throws JsonBuilderException {
        HistogramFacetConfigDTO histogramFacetConfigDTO = ParameterUtils.getParameter(parameters, "config", null, HistogramFacetConfigDTO.class);
        if(histogramFacetConfigDTO == null) {
            histogramFacetConfigDTO = JsonBuilder.create().
                    classpath("com/quasiris/qsf/elastic/config/default-histogram-facet-config.json").
                    get(HistogramFacetConfigDTO.class);
        }
        return histogramFacetConfigDTO;
    }

    public static JsonNode getIntervalJson(SearchFilter timestampFilter, List<IntervalDTO> intervalConfigList) throws JsonBuilderException {

        IntervalDTO interval = getInterval(timestampFilter, intervalConfigList);
        if(interval == null) {
            return JsonBuilder.create().object("calendar_interval", "hour").get();
        }
        return JsonBuilder.create().object(interval.getType(), interval.getInterval()).get() ;
    }

    public static IntervalDTO getInterval(SearchFilter timestampFilter, List<IntervalDTO> intervalConfigList) throws JsonBuilderException {

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
        return interval;
    }

    public static long getMinutesBetween(String from, String to) {
        Instant fromInstant = SupportedDateFormatsParser.requireInstantFromString(from);
        Instant toInstant = SupportedDateFormatsParser.requireInstantFromString(to);
        long minutesBetween  = Duration.between(fromInstant, toInstant).toMinutes();
        return minutesBetween;
    }

}
