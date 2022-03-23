package com.quasiris.qsf.test.service;

import com.quasiris.qsf.TestHelper;
import com.quasiris.qsf.test.dto.Status;
import com.quasiris.qsf.test.dto.TestCase;
import com.quasiris.qsf.test.dto.TestCaseResult;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;

import static org.mockito.Mockito.*;

class TestExecuterTest {

    @Test
    void executeMinimal() throws IOException {
        TestCase testCase = TestHelper.getResourceAsInstanse
                ("/com/quasiris/qsf/test/TestExecuter/minimal.json", TestCase.class);
        String responseString = TestHelper.getResourceAsString
                ("/com/quasiris/qsf/test/TestExecuter/minimal-response.json");
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class, RETURNS_DEEP_STUBS);
        try (
                MockedStatic<HttpClients> httpClient = mockStatic(HttpClients.class);
                MockedStatic<EntityUtils> entityUtilsMockedStatic = mockStatic(EntityUtils.class);
        ) {
            httpClient.when(HttpClients::createDefault).thenReturn(httpClientMock);
            entityUtilsMockedStatic.when(() -> EntityUtils.toString(any())).thenReturn(responseString);
            when(httpClientMock.execute(any())).thenReturn(response);
            when(response.getCode()).thenReturn(200);

            TestExecuter testExecuter = new TestExecuter(testCase);
            Assertions.assertDoesNotThrow(() -> testExecuter.execute());
        }
    }

    @Test
    void executeMinimal400Response() throws IOException {
        TestCase testCase = TestHelper.getResourceAsInstanse
                ("/com/quasiris/qsf/test/TestExecuter/minimal.json", TestCase.class);
        String responseString = TestHelper.getResourceAsString
                ("/com/quasiris/qsf/test/TestExecuter/minimal-response.json");
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class, RETURNS_DEEP_STUBS);
        try (
                MockedStatic<HttpClients> httpClient = mockStatic(HttpClients.class);
                MockedStatic<EntityUtils> entityUtilsMockedStatic = mockStatic(EntityUtils.class);
        ) {
            httpClient.when(HttpClients::createDefault).thenReturn(httpClientMock);
            entityUtilsMockedStatic.when(() -> EntityUtils.toString(any())).thenReturn(responseString);
            when(httpClientMock.execute(any())).thenReturn(response);
            when(response.getCode()).thenReturn(400);

            TestExecuter testExecuter = new TestExecuter(testCase);
            Assertions.assertThrows(RuntimeException.class, () -> testExecuter.execute());
        }
    }

    @Test
    void executeSearchResponseSuccess() throws IOException {
        TestCase testCase = TestHelper.getResourceAsInstanse
                ("/com/quasiris/qsf/test/TestExecuter/search1.json", TestCase.class);
        String responseString = TestHelper.getResourceAsString
                ("/com/quasiris/qsf/test/TestExecuter/search-response1.json");
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class, RETURNS_DEEP_STUBS);
        try (
                MockedStatic<HttpClients> httpClient = mockStatic(HttpClients.class);
                MockedStatic<EntityUtils> entityUtilsMockedStatic = mockStatic(EntityUtils.class);
        ) {
            httpClient.when(HttpClients::createDefault).thenReturn(httpClientMock);
            entityUtilsMockedStatic.when(() -> EntityUtils.toString(any())).thenReturn(responseString);
            when(httpClientMock.execute(any())).thenReturn(response);
            when(response.getCode()).thenReturn(200);

            TestExecuter testExecuter = new TestExecuter(testCase);
            TestCaseResult actual = testExecuter.execute();
            Assertions.assertEquals(3, actual.getAssertionResults().size());
            Assertions.assertTrue(actual.getAssertionResults().stream().allMatch(a -> a.getStatus() == Status.SUCCESS));
        }
    }

    @Test
    void executeSearchResponseErrors() throws IOException {
        TestCase testCase = TestHelper.getResourceAsInstanse
                ("/com/quasiris/qsf/test/TestExecuter/search2.json", TestCase.class);
        String responseString = TestHelper.getResourceAsString
                ("/com/quasiris/qsf/test/TestExecuter/search-response2.json");
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class, RETURNS_DEEP_STUBS);
        try (
                MockedStatic<HttpClients> httpClient = mockStatic(HttpClients.class);
                MockedStatic<EntityUtils> entityUtilsMockedStatic = mockStatic(EntityUtils.class);
        ) {
            httpClient.when(HttpClients::createDefault).thenReturn(httpClientMock);
            entityUtilsMockedStatic.when(() -> EntityUtils.toString(any())).thenReturn(responseString);
            when(httpClientMock.execute(any())).thenReturn(response);
            when(response.getCode()).thenReturn(200);

            TestExecuter testExecuter = new TestExecuter(testCase);
            TestCaseResult actual = testExecuter.execute();

            Assertions.assertEquals(2, actual.getAssertionResults().size());
            Assertions.assertTrue(actual.getAssertionResults().stream().allMatch(a -> a.getStatus() == Status.SUCCESS));
        }
    }

    @Test
    void executeSearchResponseResultAndDidYouMeanErrors() throws IOException {
        TestCase testCase = TestHelper.getResourceAsInstanse
                ("/com/quasiris/qsf/test/TestExecuter/search3.json", TestCase.class);
        String responseString = TestHelper.getResourceAsString
                ("/com/quasiris/qsf/test/TestExecuter/search-response3.json");
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class, RETURNS_DEEP_STUBS);
        try (
                MockedStatic<HttpClients> httpClient = mockStatic(HttpClients.class);
                MockedStatic<EntityUtils> entityUtilsMockedStatic = mockStatic(EntityUtils.class);
        ) {
            httpClient.when(HttpClients::createDefault).thenReturn(httpClientMock);
            entityUtilsMockedStatic.when(() -> EntityUtils.toString(any())).thenReturn(responseString);
            when(httpClientMock.execute(any())).thenReturn(response);
            when(response.getCode()).thenReturn(200);

            TestExecuter testExecuter = new TestExecuter(testCase);
            TestCaseResult actual = testExecuter.execute();

            TestHelper.logStructure(actual.getAssertionResults());
            Assertions.assertEquals(3, actual.getAssertionResults().size());
            Assertions.assertEquals(2, actual.getAssertionResults().stream()
                    .filter(a -> a.getStatus() == Status.FAILED).count());
            Assertions.assertEquals(1, actual.getAssertionResults().stream()
                    .filter(a -> a.getStatus() == Status.SUCCESS).count());
        }
    }

    @Test
    void executeSearchResponseDidYouMeanNotMatches() throws IOException {
        TestCase testCase = TestHelper.getResourceAsInstanse
                ("/com/quasiris/qsf/test/TestExecuter/search4.json", TestCase.class);
        String responseString = TestHelper.getResourceAsString
                ("/com/quasiris/qsf/test/TestExecuter/search-response4.json");
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class, RETURNS_DEEP_STUBS);
        try (
                MockedStatic<HttpClients> httpClient = mockStatic(HttpClients.class);
                MockedStatic<EntityUtils> entityUtilsMockedStatic = mockStatic(EntityUtils.class);
        ) {
            httpClient.when(HttpClients::createDefault).thenReturn(httpClientMock);
            entityUtilsMockedStatic.when(() -> EntityUtils.toString(any())).thenReturn(responseString);
            when(httpClientMock.execute(any())).thenReturn(response);
            when(response.getCode()).thenReturn(200);

            TestExecuter testExecuter = new TestExecuter(testCase);
            TestCaseResult actual = testExecuter.execute();

            TestHelper.logStructure(actual.getAssertionResults());
            Assertions.assertEquals(2, actual.getAssertionResults().size());
            Assertions.assertEquals(1, actual.getAssertionResults().stream()
                    .filter(a -> a.getStatus() == Status.FAILED).count());
            Assertions.assertEquals(1, actual.getAssertionResults().stream()
                    .filter(a -> a.getStatus() == Status.SUCCESS).count());
        }
    }

    @Test
    void executeSearchResponseNoKeyForResponse() throws IOException {
        TestCase testCase = TestHelper.getResourceAsInstanse
                ("/com/quasiris/qsf/test/TestExecuter/search5.json", TestCase.class);
        String responseString = TestHelper.getResourceAsString
                ("/com/quasiris/qsf/test/TestExecuter/search-response5.json");
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class, RETURNS_DEEP_STUBS);
        try (
                MockedStatic<HttpClients> httpClient = mockStatic(HttpClients.class);
                MockedStatic<EntityUtils> entityUtilsMockedStatic = mockStatic(EntityUtils.class);
        ) {
            httpClient.when(HttpClients::createDefault).thenReturn(httpClientMock);
            entityUtilsMockedStatic.when(() -> EntityUtils.toString(any())).thenReturn(responseString);
            when(httpClientMock.execute(any())).thenReturn(response);
            when(response.getCode()).thenReturn(200);

            TestExecuter testExecuter = new TestExecuter(testCase);
            TestCaseResult actual = testExecuter.execute();

            TestHelper.logStructure(actual);
            Assertions.assertEquals(3, actual.getAssertionResults().size());
            Assertions.assertEquals(2, actual.getAssertionResults().stream().filter(a -> a.getStatus() == Status.SUCCESS).count());
            Assertions.assertEquals(1, actual.getAssertionResults().stream().filter(a -> a.getStatus() == Status.FAILED).count());
        }
    }

    @Test
    void executeJsonPathSuccess() throws IOException {
        TestCase testCase = TestHelper.getResourceAsInstanse
                ("/com/quasiris/qsf/test/TestExecuter/json-path1.json", TestCase.class);
        String responseString = TestHelper.getResourceAsString
                ("/com/quasiris/qsf/test/TestExecuter/json-path-response1.json");
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class, RETURNS_DEEP_STUBS);
        try (
                MockedStatic<HttpClients> httpClient = mockStatic(HttpClients.class);
                MockedStatic<EntityUtils> entityUtilsMockedStatic = mockStatic(EntityUtils.class);
        ) {
            httpClient.when(HttpClients::createDefault).thenReturn(httpClientMock);
            entityUtilsMockedStatic.when(() -> EntityUtils.toString(any())).thenReturn(responseString);
            when(httpClientMock.execute(any())).thenReturn(response);
            when(response.getCode()).thenReturn(200);

            TestExecuter testExecuter = new TestExecuter(testCase);
            TestCaseResult actual = testExecuter.execute();

            TestHelper.logStructure(actual);
            Assertions.assertEquals(2, actual.getAssertionResults().size());
            Assertions.assertEquals(2, actual.getAssertionResults().stream().filter(a -> a.getStatus() == Status.SUCCESS).count());
        }
    }

    @Test
    void executeJsonPathFail() throws IOException {
        TestCase testCase = TestHelper.getResourceAsInstanse
                ("/com/quasiris/qsf/test/TestExecuter/json-path2.json", TestCase.class);
        String responseString = TestHelper.getResourceAsString
                ("/com/quasiris/qsf/test/TestExecuter/json-path-response2.json");
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class, RETURNS_DEEP_STUBS);
        try (
                MockedStatic<HttpClients> httpClient = mockStatic(HttpClients.class);
                MockedStatic<EntityUtils> entityUtilsMockedStatic = mockStatic(EntityUtils.class);
        ) {
            httpClient.when(HttpClients::createDefault).thenReturn(httpClientMock);
            entityUtilsMockedStatic.when(() -> EntityUtils.toString(any())).thenReturn(responseString);
            when(httpClientMock.execute(any())).thenReturn(response);
            when(response.getCode()).thenReturn(200);

            TestExecuter testExecuter = new TestExecuter(testCase);
            TestCaseResult actual = testExecuter.execute();

            TestHelper.logStructure(actual);
            Assertions.assertEquals(2, actual.getAssertionResults().size());
            Assertions.assertEquals(1, actual.getAssertionResults().stream().filter(a -> a.getStatus() == Status.SUCCESS).count());
            Assertions.assertEquals(1, actual.getAssertionResults().stream().filter(a -> a.getStatus() == Status.FAILED).count());
        }
    }


    @Test
    void executeHttpSuccess() throws IOException {
        TestCase testCase = TestHelper.getResourceAsInstanse
                ("/com/quasiris/qsf/test/TestExecuter/http1.json", TestCase.class);
        String responseString = TestHelper.getResourceAsString
                ("/com/quasiris/qsf/test/TestExecuter/http-response1.json");
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class, RETURNS_DEEP_STUBS);
        try (
                MockedStatic<HttpClients> httpClient = mockStatic(HttpClients.class);
                MockedStatic<EntityUtils> entityUtilsMockedStatic = mockStatic(EntityUtils.class);
        ) {
            httpClient.when(HttpClients::createDefault).thenReturn(httpClientMock);
            entityUtilsMockedStatic.when(() -> EntityUtils.toString(any())).thenReturn(responseString);
            when(httpClientMock.execute(any())).thenReturn(response);
            when(response.getCode()).thenReturn(200);

            TestExecuter testExecuter = new TestExecuter(testCase);
            TestCaseResult actual = testExecuter.execute();

            TestHelper.logStructure(actual);
            Assertions.assertEquals(1, actual.getAssertionResults().size());
            Assertions.assertEquals(1, actual.getAssertionResults().stream().filter(a -> a.getStatus() == Status.SUCCESS).count());
        }
    }

    @Test
    void executeHttpFail() throws IOException {
        TestCase testCase = TestHelper.getResourceAsInstanse
                ("/com/quasiris/qsf/test/TestExecuter/http1.json", TestCase.class);
        String responseString = TestHelper.getResourceAsString
                ("/com/quasiris/qsf/test/TestExecuter/http-response1.json");
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class, RETURNS_DEEP_STUBS);
        try (
                MockedStatic<HttpClients> httpClient = mockStatic(HttpClients.class);
                MockedStatic<EntityUtils> entityUtilsMockedStatic = mockStatic(EntityUtils.class);
        ) {
            httpClient.when(HttpClients::createDefault).thenReturn(httpClientMock);
            entityUtilsMockedStatic.when(() -> EntityUtils.toString(any())).thenReturn(responseString);
            when(httpClientMock.execute(any())).thenReturn(response);
            when(response.getCode()).thenReturn(201);

            TestExecuter testExecuter = new TestExecuter(testCase);
            TestCaseResult actual = testExecuter.execute();

            TestHelper.logStructure(actual);
            Assertions.assertEquals(1, actual.getAssertionResults().size());
            Assertions.assertEquals(1, actual.getAssertionResults().stream().filter(a -> a.getStatus() == Status.FAILED).count());
        }
    }
}