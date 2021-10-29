package com.quasiris.qsf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static java.time.temporal.ChronoUnit.SECONDS;


public class TestHelper {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(TestHelper.class);


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

    public static void checkNearlySameTime(String actual, Instant expected, long difference) {
        log.info("checkNearlySameTime: actual = {}", actual);
        log.info("checkNearlySameTime: expected = {}", expected);
        Instant actualValue = DATE_TIME_FORMATTER.parse(actual, Instant::from);
        long actualDifference = SECONDS.between(actualValue, expected);
        Assertions.assertTrue(difference >= actualDifference);
    }

    public static String getResourceAsString(String filePath) throws IOException {
        try (InputStream is = TestHelper.class.getResourceAsStream(filePath)) {
            return IOUtils.toString(is, Charset.defaultCharset());
        }
    }

    public static Object getResourceAsObject(String filePath) throws IOException {
        try (InputStream is = TestHelper.class.getResourceAsStream(filePath)) {
            return objectMapper.readValue(is, Object.class);
        }
    }

    public static void logStructure(Object obj) throws JsonProcessingException {
        String result = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        log.info("obj = {}", result);
    }

    public static <T> T getResourceAsInstanse(String filePath, Class<T> resultClass) throws IOException {
        try (InputStream is = TestHelper.class.getResourceAsStream(filePath)) {
            return objectMapper.readValue(is, resultClass);
        }
    }
}
