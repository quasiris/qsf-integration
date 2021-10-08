package com.quasiris.qsf.response;

import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.MonitoringResponse;
import com.quasiris.qsf.dto.response.SearchResult;
import com.quasiris.qsf.monitoring.MonitoringDocument;
import com.quasiris.qsf.monitoring.MonitoringStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

class MonitoringResponseFactoryTest {

    @Test
    void testCreateWithoutDocs() {
        long time = 24L;
        String name = "name";
        long total = 300L;
        int statusCode = 200;
        String status = MonitoringStatus.OK;

        SearchResult searchResult = new SearchResult();
        searchResult.setTime(time);
        searchResult.setName(name);
        searchResult.setTotal(total);
        searchResult.setStatusCode(statusCode);
        MonitoringResponse monitoringResponse = MonitoringResponseFactory.create(searchResult, status);
        Assertions.assertEquals(monitoringResponse.getStatus(), status);
        Assertions.assertEquals(monitoringResponse.getStatusCode(), statusCode);
        Assertions.assertEquals(monitoringResponse.getTime(), time);
        checkNearlySameTime(monitoringResponse.getCurrentTime());
        Assertions.assertNull(monitoringResponse.getResult());
    }

    @Test
    void testCreateWithDocs() {
        long time = 24L;
        String name = "name";
        long total = 300L;
        int statusCode = 200;
        String status = MonitoringStatus.OK;

        SearchResult searchResult = new SearchResult();
        searchResult.setTime(time);
        searchResult.setName(name);
        searchResult.setTotal(total);
        Document document1 = new Document("1", new HashMap<String, Object>() {{
            put("1", 1);
            put("2", 2);
        }});
        Document document2 = new Document("2", new HashMap<String, Object>() {{
            put("1", 1);
            put("2", 2);
        }});
        searchResult.setDocuments(Arrays.asList(document1, document2));
        searchResult.setStatusCode(statusCode);
        MonitoringResponse monitoringResponse = MonitoringResponseFactory.create(searchResult, status);
        Assertions.assertEquals(status, monitoringResponse.getStatus());
        Assertions.assertEquals(statusCode, monitoringResponse.getStatusCode());
        Assertions.assertEquals(time, monitoringResponse.getTime());
        checkNearlySameTime(monitoringResponse.getCurrentTime());
        Assertions.assertEquals(document1.getDocument(), monitoringResponse.getResult().get(0));
        Assertions.assertEquals(document2.getDocument(), monitoringResponse.getResult().get(1));
    }

    private void checkNearlySameTime(Date currentTime) {
        Date minus1MinDate = new Date(System.currentTimeMillis() - 60 * 1000);
        Date plus1MinDate = new Date(System.currentTimeMillis() + 60 * 1000);
        Assertions.assertTrue(currentTime.compareTo(minus1MinDate) > 0 &&
                currentTime.compareTo(plus1MinDate) < 0);
    }

    @Test
    void testCreateNullInputNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> MonitoringResponseFactory.create(null, "2"));
    }

    @Test
    void testCreateWithMonitoringDocumentWithDoc() {
        long time = 24L;
        MonitoringDocument<String> monObj = new MonitoringDocument<>();
        monObj.setValue("My value");
        monObj.setStatus(MonitoringStatus.OK);
        monObj.setMonitoringId("3445dfg");
        HashMap<String, Object> doc = new HashMap<String, Object>() {{
            put("1", 3);
            put("2", 2);
        }};
        monObj.setDocument(doc);
        MonitoringResponse actual = MonitoringResponseFactory.create(monObj, time);
        checkNearlySameTime(actual.getCurrentTime());
        Assertions.assertEquals(time, actual.getTime());
        Assertions.assertEquals(monObj.getStatus(), actual.getStatus());
        Assertions.assertEquals(200, actual.getStatusCode());
    }

    @Test
    void testCreateWithMonitoringDocumentNullInputNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> MonitoringResponseFactory.create(null, 24L));
    }

    private MonitoringResponse createMonitoringResponse(Map<String, Object> result) {
        long time = 24L;
        MonitoringDocument<String> monObj = new MonitoringDocument<>();
        monObj.setDocument(result);
        return MonitoringResponseFactory.create(monObj, time);
    }

    private MonitoringResponse createMonitoringResponse(String status) {
        long time = 24L;
        MonitoringDocument<String> monObj = new MonitoringDocument<>();
        monObj.setStatus(status);
        return MonitoringResponseFactory.create(monObj, time);
    }

    @Test
    void testmergeLeftRightCheckDocs() {
        HashMap<String, Object> fstResult = new HashMap<String, Object>() {{
            put("1", 1);
            put("2", 2);
        }};
        MonitoringResponse left = createMonitoringResponse(fstResult);
        HashMap<String, Object> secResult = new HashMap<String, Object>() {{
            put("3", 3);
            put("4", 4);
        }};
        MonitoringResponse right = createMonitoringResponse(secResult);

        HashMap<String, Object> fstExpected = new HashMap<>(fstResult);
        HashMap<String, Object> secExpected = new HashMap<>(secResult);
        MonitoringResponse actual = MonitoringResponseFactory.merge(left, right);
        Assertions.assertEquals(actual.getResult(), Arrays.asList(fstExpected, secExpected));

    }


    private static Stream<Arguments> testmergeLeftRightCheckStatusSource() {
        return Stream.of(
                Arguments.of(MonitoringStatus.OK, MonitoringStatus.OK, MonitoringStatus.OK),
                Arguments.of(MonitoringStatus.ERROR, MonitoringStatus.ERROR, MonitoringStatus.ERROR),
                Arguments.of(MonitoringStatus.OK, MonitoringStatus.ERROR, MonitoringStatus.ERROR),
                Arguments.of(MonitoringStatus.OK, MonitoringStatus.WARN, MonitoringStatus.WARN),
                Arguments.of(MonitoringStatus.WARN, MonitoringStatus.ERROR, MonitoringStatus.ERROR)
        );
    }

    @ParameterizedTest
    @MethodSource("testmergeLeftRightCheckStatusSource")
    void testmergeLeftRightCheckStatus(String status1, String status2, String statusResult) {
        MonitoringResponse mobj1 = createMonitoringResponse(status1);
        MonitoringResponse mobj2 = createMonitoringResponse(status2);
        MonitoringResponse actual = MonitoringResponseFactory.merge(mobj1, mobj2);
        Assertions.assertEquals(statusResult, actual.getStatus());
    }

    private static Stream<Arguments> testMergeMultipleCheckStatusSource() {
        return Stream.of(
                Arguments.of(Arrays.asList(MonitoringStatus.OK, MonitoringStatus.OK, MonitoringStatus.OK), MonitoringStatus.OK),
                Arguments.of(Arrays.asList(MonitoringStatus.OK, MonitoringStatus.WARN, MonitoringStatus.ERROR), MonitoringStatus.ERROR),
                Arguments.of(Arrays.asList(MonitoringStatus.OK, MonitoringStatus.WARN, MonitoringStatus.WARN), MonitoringStatus.WARN)
        );
    }

    @ParameterizedTest
    @MethodSource("testMergeMultipleCheckStatusSource")
    void testMergeMultipleCheckStatus(List<String> statuses, String result) {
        MonitoringResponse[] monitoringResponses = statuses.stream()
                .map(this::createMonitoringResponse)
                .toArray(MonitoringResponse[]::new);
        MonitoringResponse actual = MonitoringResponseFactory.merge(monitoringResponses);
        Assertions.assertEquals(result, actual.getStatus());
    }


    @Test
    void testmergeMultipleCheckDocs() {
        HashMap<String, Object> doc1 = new HashMap<String, Object>() {{
            put("1", 1);
            put("2", 2);
        }};
        MonitoringResponse res1 = createMonitoringResponse(doc1);
        HashMap<String, Object> doc2 = new HashMap<String, Object>() {{
            put("3", 3);
            put("4", 4);
        }};
        MonitoringResponse resp2 = createMonitoringResponse(doc2);

        HashMap<String, Object> doc3 = new HashMap<String, Object>() {{
            put("5", null);
        }};
        MonitoringResponse resp3 = createMonitoringResponse(doc3);


        HashMap<String, Object> exp1 = new HashMap<>(doc1);
        HashMap<String, Object> exp2 = new HashMap<>(doc2);
        HashMap<String, Object> exp3 = new HashMap<>(doc3);
        MonitoringResponse actual = MonitoringResponseFactory.merge(res1, resp2, resp3);
        Assertions.assertEquals(actual.getResult(), Arrays.asList(exp1, exp2, exp3));

    }
}