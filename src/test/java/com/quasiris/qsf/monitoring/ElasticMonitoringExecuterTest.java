package com.quasiris.qsf.monitoring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasiris.qsf.TestHelper;
import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.MonitoringResponse;
import com.quasiris.qsf.dto.response.SearchResult;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerDebugException;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.pipeline.PipelineExecuter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ElasticMonitoringExecuterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @Test
    void doMonitoringEmptyMonitoringList() throws PipelineContainerDebugException, JsonProcessingException, PipelineContainerException {

        String baseUrl = "http://localhost:8083";

        PipelineExecuter mockExecutor = mock(PipelineExecuter.class);
        PipelineContainer mockPipelineContainer = mock(PipelineContainer.class, RETURNS_DEEP_STUBS);
        try (MockedStatic<PipelineExecuter> staticExecutorMock = Mockito.mockStatic(PipelineExecuter.class)) {
            staticExecutorMock.when(PipelineExecuter::create).thenReturn(mockExecutor);
            when(mockExecutor.pipeline(any())).thenReturn(mockExecutor);
            when(mockExecutor.searchQuery(any())).thenReturn(mockExecutor);
            when(mockExecutor.execute()).thenReturn(mockPipelineContainer);
            SearchResult searchResult = new SearchResult();
            searchResult.setTime(0L);
            when(mockPipelineContainer.getSearchResults().get(anyString())).thenReturn(searchResult);

            ElasticMonitoringExecuter elasticMonitoringExecuter = new ElasticMonitoringExecuter(baseUrl, new ArrayList<>());
            MonitoringResponse monitoringResponse = elasticMonitoringExecuter.doMonitoring();
            System.out.println("monitoringResponse = " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(monitoringResponse));
            Assertions.assertEquals(0, monitoringResponse.getTime());
            Assertions.assertEquals("OK", monitoringResponse.getStatus());
        }
    }

    @Test
    void doMonitoringNotEmptyMonitoringNotActiveListOk() throws PipelineContainerDebugException, JsonProcessingException, PipelineContainerException {
        long time = 0L;
        long error = 0;
        long warn = 0;

        List<MonitoringDocument> monitoringList = MonitoringBuilder.aMonitoring()
                .active(false)
                .totalHits(error, warn)
                .build();

        String baseUrl = "http://localhost:8083";

        PipelineExecuter mockExecutor = mock(PipelineExecuter.class);
        PipelineContainer mockPipelineContainer = mock(PipelineContainer.class, RETURNS_DEEP_STUBS);
        try (MockedStatic<PipelineExecuter> staticExecutorMock = Mockito.mockStatic(PipelineExecuter.class)) {
            staticExecutorMock.when(PipelineExecuter::create).thenReturn(mockExecutor);
            when(mockExecutor.pipeline(any())).thenReturn(mockExecutor);
            when(mockExecutor.searchQuery(any())).thenReturn(mockExecutor);
            when(mockExecutor.execute()).thenReturn(mockPipelineContainer);
            SearchResult searchResult = new SearchResult();
            searchResult.setTime(time);
            when(mockPipelineContainer.getSearchResults().get(anyString())).thenReturn(searchResult);

            ElasticMonitoringExecuter elasticMonitoringExecuter = new ElasticMonitoringExecuter("some", new ArrayList<>());
            elasticMonitoringExecuter.setBaseUrl(baseUrl);
            elasticMonitoringExecuter.setMonitoringDocumentList(monitoringList);
            elasticMonitoringExecuter.setTimeout(400L);
            elasticMonitoringExecuter.setQuery("*");

            MonitoringResponse monitoringResponse = elasticMonitoringExecuter.doMonitoring();
            System.out.println("monitoringResponse = " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(monitoringResponse));

            Assertions.assertNotNull(monitoringResponse.getResult());
            Assertions.assertEquals("OK", monitoringResponse.getStatus());
            Assertions.assertEquals(error, monitoringResponse.getResult().get(0).get("minErrorLimit"));
            Assertions.assertEquals(warn, monitoringResponse.getResult().get(0).get("minWarnLimit"));
            Assertions.assertEquals("Long", monitoringResponse.getResult().get(0).get("dataType"));
            Assertions.assertEquals(elasticMonitoringExecuter.getBaseUrl(), monitoringResponse.getResult().get(0).get("baseUrl"));
            Assertions.assertEquals(monitoringList.size(), monitoringResponse.getResult().size());
        }
    }

    @Test
    void doMonitoringNotEmptyMonitoringProcessingTimeFullNotActiveListOk() throws PipelineContainerDebugException, JsonProcessingException, PipelineContainerException {
        long time = 0;
        int error = 0;
        int warn = 0;

        List<MonitoringDocument> monitoringList = MonitoringBuilder.aMonitoring()
                .active(false)
                .processingTimeFull(error, warn, false)
                .build();

        String baseUrl = "http://localhost:8083";

        PipelineExecuter mockExecutor = mock(PipelineExecuter.class);
        PipelineContainer mockPipelineContainer = mock(PipelineContainer.class, RETURNS_DEEP_STUBS);
        try (MockedStatic<PipelineExecuter> staticExecutorMock = Mockito.mockStatic(PipelineExecuter.class)) {
            staticExecutorMock.when(PipelineExecuter::create).thenReturn(mockExecutor);
            when(mockExecutor.pipeline(any())).thenReturn(mockExecutor);
            when(mockExecutor.searchQuery(any())).thenReturn(mockExecutor);
            when(mockExecutor.execute()).thenReturn(mockPipelineContainer);
            SearchResult searchResult = new SearchResult();
            searchResult.setTime(time);
            searchResult.setDocuments(new ArrayList<>());
            OffsetDateTime now = OffsetDateTime.now();
            String resultDateString = TestHelper.DATE_TIME_FORMATTER.format(now);
            searchResult.getDocuments().add(new Document("lkeickdf", new HashMap<String, Object>() {{
                put("key", "value");
                put("processingtime", resultDateString);
            }}));
            when(mockPipelineContainer.getSearchResults().get(anyString())).thenReturn(searchResult);

            ElasticMonitoringExecuter elasticMonitoringExecuter = new ElasticMonitoringExecuter("some", new ArrayList<>());
            elasticMonitoringExecuter.setBaseUrl(baseUrl);
            elasticMonitoringExecuter.setMonitoringDocumentList(monitoringList);
            elasticMonitoringExecuter.setTimeout(400L);
            elasticMonitoringExecuter.setQuery("*");

            MonitoringResponse monitoringResponse = elasticMonitoringExecuter.doMonitoring();

            Assertions.assertNotNull(monitoringResponse.getResult());
            Assertions.assertEquals("OK", monitoringResponse.getStatus());
            TestHelper.checkNearlySameTime(resultDateString, (Instant) monitoringResponse.getResult().get(0).get("minErrorLimit"), 100);
            TestHelper.checkNearlySameTime(resultDateString, (Instant) monitoringResponse.getResult().get(0).get("minWarnLimit"), 100);
            Assertions.assertEquals("Instant", monitoringResponse.getResult().get(0).get("dataType"));
            Assertions.assertEquals(elasticMonitoringExecuter.getBaseUrl(), monitoringResponse.getResult().get(0).get("baseUrl"));
            Assertions.assertEquals(monitoringList.size(), monitoringResponse.getResult().size());
        }
    }

    @Test
    void doMonitoringNotEmptyMonitoringProcessingUpdateTimeFullNotActiveListOk() throws PipelineContainerDebugException, JsonProcessingException, PipelineContainerException {
        long time = 0;
        int error = 0;
        int warn = 0;

        List<MonitoringDocument> monitoringList = MonitoringBuilder.aMonitoring()
                .active(false)
                .processingTimeUpdate(error, warn, false)
                .build();

        String baseUrl = "http://localhost:8083";

        PipelineExecuter mockExecutor = mock(PipelineExecuter.class);
        PipelineContainer mockPipelineContainer = mock(PipelineContainer.class, RETURNS_DEEP_STUBS);
        try (MockedStatic<PipelineExecuter> staticExecutorMock = Mockito.mockStatic(PipelineExecuter.class)) {
            staticExecutorMock.when(PipelineExecuter::create).thenReturn(mockExecutor);
            when(mockExecutor.pipeline(any())).thenReturn(mockExecutor);
            when(mockExecutor.searchQuery(any())).thenReturn(mockExecutor);
            when(mockExecutor.execute()).thenReturn(mockPipelineContainer);
            SearchResult searchResult = new SearchResult();
            searchResult.setTime(time);
            searchResult.setDocuments(new ArrayList<>());
            OffsetDateTime now = OffsetDateTime.now();
            String resultDateString = TestHelper.DATE_TIME_FORMATTER.format(now);
            searchResult.getDocuments().add(new Document("lkeickdf", new HashMap<String, Object>() {{
                put("key", "value");
                put("processingtime", resultDateString);
            }}));
            when(mockPipelineContainer.getSearchResults().get(anyString())).thenReturn(searchResult);

            ElasticMonitoringExecuter elasticMonitoringExecuter = new ElasticMonitoringExecuter("some", new ArrayList<>());
            elasticMonitoringExecuter.setBaseUrl(baseUrl);
            elasticMonitoringExecuter.setMonitoringDocumentList(monitoringList);
            elasticMonitoringExecuter.setTimeout(400L);
            elasticMonitoringExecuter.setQuery("*");

            MonitoringResponse monitoringResponse = elasticMonitoringExecuter.doMonitoring();

            Assertions.assertNotNull(monitoringResponse.getResult());
            Assertions.assertEquals("OK", monitoringResponse.getStatus());
            TestHelper.checkNearlySameTime(resultDateString, (Instant) monitoringResponse.getResult().get(0).get("minErrorLimit"), 100);
            TestHelper.checkNearlySameTime(resultDateString, (Instant) monitoringResponse.getResult().get(0).get("minWarnLimit"), 100);
            Assertions.assertEquals("Instant", monitoringResponse.getResult().get(0).get("dataType"));
            Assertions.assertEquals(elasticMonitoringExecuter.getBaseUrl(), monitoringResponse.getResult().get(0).get("baseUrl"));
            Assertions.assertEquals(monitoringList.size(), monitoringResponse.getResult().size());
        }
    }
}