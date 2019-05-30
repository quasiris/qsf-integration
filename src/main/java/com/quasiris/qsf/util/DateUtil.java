package com.quasiris.qsf.util;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A util for converting dates.
 */
public class DateUtil {

    /**
     * Creates a instant by a date string.
     * @param date the date as string.
     * @return the created instant.
     */
    public static Instant getInstantByString(String date) {
        OffsetDateTime ld = OffsetDateTime.parse(date);
        return ld.toInstant();
    }

    /**
     * Creates a instant by a elastic date string.
     * @param date the date as string.
     * @return the created instant.
     */
    public static Instant getInstantByElasticDate(String date) {
        OffsetDateTime ld = OffsetDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
        return ld.toInstant();
    }
}
