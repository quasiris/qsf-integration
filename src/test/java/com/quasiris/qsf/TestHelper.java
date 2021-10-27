package com.quasiris.qsf;

import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;

import static java.time.temporal.ChronoUnit.SECONDS;


public class TestHelper {

    private static Logger log = LoggerFactory.getLogger(TestHelper.class);


    public static void checkNearlySameTime(Date currentTime) {
        Date minus1MinDate = new Date(System.currentTimeMillis() - 60 * 1000);
        Date plus1MinDate = new Date(System.currentTimeMillis() + 60 * 1000);
        Assertions.assertTrue(currentTime.compareTo(minus1MinDate) > 0 &&
                currentTime.compareTo(plus1MinDate) < 0);
    }

    public static void checkNearlySameTime(Instant actual, Instant expected, long difference) {
        log.info("checkNearlySameTime: actual = {}", actual);
        log.info("checkNearlySameTime: expected = {}", expected);
        long actualDifference = SECONDS.between(actual, expected);
        Assertions.assertTrue(difference >= actualDifference);
    }
}
