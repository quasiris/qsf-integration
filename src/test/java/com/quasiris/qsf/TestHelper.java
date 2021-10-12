package com.quasiris.qsf;

import org.junit.jupiter.api.Assertions;

import java.util.Date;

public class TestHelper {

    public static void checkNearlySameTime(Date currentTime) {
        Date minus1MinDate = new Date(System.currentTimeMillis() - 60 * 1000);
        Date plus1MinDate = new Date(System.currentTimeMillis() + 60 * 1000);
        Assertions.assertTrue(currentTime.compareTo(minus1MinDate) > 0 &&
                currentTime.compareTo(plus1MinDate) < 0);
    }
}
