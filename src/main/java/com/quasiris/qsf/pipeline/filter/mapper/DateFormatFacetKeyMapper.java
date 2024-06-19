package com.quasiris.qsf.pipeline.filter.mapper;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateFormatFacetKeyMapper implements FacetKeyMapper {

    private DateTimeFormatter formatter;

    private ZoneId zoneId;

    public DateFormatFacetKeyMapper(String pattern) {
        this.formatter = DateTimeFormatter.ofPattern(pattern);
        this.zoneId = ZoneId.of("Europe/Berlin");
    }

    public DateFormatFacetKeyMapper(String pattern, String zoneId) {
        this.formatter = DateTimeFormatter.ofPattern(pattern);
        this.zoneId = ZoneId.of(zoneId);
    }

    @Override
    public String map(String value) {

        final String formattedDate = Instant.ofEpochMilli(Long.valueOf(value))
                .atZone(zoneId)
                .format(formatter);
        return formattedDate;
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    public void setFormatter(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }
}
