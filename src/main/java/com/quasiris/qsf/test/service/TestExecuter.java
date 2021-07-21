package com.quasiris.qsf.test.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.Facet;
import com.quasiris.qsf.dto.response.FacetValue;
import com.quasiris.qsf.dto.response.SearchResponse;
import com.quasiris.qsf.dto.response.SearchResult;
import com.quasiris.qsf.dto.response.SingleSearchResponse;
import com.quasiris.qsf.test.builder.AssertionResultBuilder;
import com.quasiris.qsf.test.dto.AssertionResult;
import com.quasiris.qsf.test.dto.Http;
import com.quasiris.qsf.test.dto.JsonPath;
import com.quasiris.qsf.test.dto.Status;
import com.quasiris.qsf.test.dto.TestCase;
import com.quasiris.qsf.test.dto.TestCaseResult;
import com.quasiris.qsf.util.DateUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class TestExecuter {

    private TestCase testCase;

    private Object rawResponseBody;

    private String responseBodyString;

    private DocumentContext responseDocumentContext;

    private CloseableHttpResponse httpResponse;

    private Long requestTime;

    private TestCaseResult testCaseResult = new TestCaseResult();

    public TestExecuter(TestCase testCase) {
        this.testCase = testCase;
    }

    public TestCaseResult execute()  {


        try {
            HttpGet httpGet = new HttpGet(testCase.getQuery().getUrl());
            execute(httpGet);

            if(testCaseResult.getAssertionResults() == null) {
                testCaseResult.setAssertionResults(new ArrayList<>());
            }
            testCaseResult.setTestCaseId(testCase.getId());

            processSearchResponse(testCase);
            processSingleSearchResponse(testCase);
            processJsonPath(testCase);
            processSuggest(testCase);
            processHttp(testCase);

            if(testCaseResult.getAssertionResults() == null || testCaseResult.getAssertionResults().size() == 0) {
                testCaseResult.getAssertionResults().add(AssertionResultBuilder.create().
                        name(globalName("no test found in test case")).
                        failed().
                        build());
            }

            return testCaseResult;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    void addAssertionResults(AssertionResult assertionResult) {
        testCaseResult.getAssertionResults().add(assertionResult);
    }

    public void processSingleSearchResponse(TestCase testCase) throws IOException {
        if(testCase.getAssertions().getSingleSearchResponse() != null) {
            SingleSearchResponse searchResponse = readValue(responseBodyString, SingleSearchResponse.class);
            rawResponseBody = searchResponse;
            for (SingleSearchResponse assertion : testCase.getAssertions().getSingleSearchResponse()) {
                addAssertionResults(assertGlobalStatusCode(assertion.getStatusCode(), searchResponse.getStatusCode()));
                // TODO
                //testCaseResult.getAssertionResults().addAll(assertionResults);
            }
        }
    }

    public void processSuggest(TestCase testCase) {
        if(testCase.getAssertions().getSuggest() != null) {
            testCaseResult.getAssertionResults().add(AssertionResultBuilder.create().
                    name(globalName("suggest")).
                    success().
                    build());
        }
    }

    public void processHttp(TestCase testCase) {
        if(testCase.getAssertions().getHttp() != null) {

            for(Http http : testCase.getAssertions().getHttp()) {
                testCaseResult.getAssertionResults().addAll(assertStringValue(
                        "httpStatusCode",
                        http.getStatusCode(),
                        String.valueOf(httpResponse.getStatusLine().getStatusCode())));

            }
        }
    }
    public void processJsonPath(TestCase testCase) {
        if(testCase.getAssertions().getJsonPath() != null) {
            testCaseResult.getAssertionResults().add(AssertionResultBuilder.create().
                    name(globalName("jsonPath")).
                    success().
                    build());

            for(JsonPath jsonPath : testCase.getAssertions().getJsonPath()) {

                String actualValue = JsonPathParser.getValueFromContext(jsonPath.getPath(), getResponseDocumentContext());
                testCaseResult.getAssertionResults().addAll(assertStringValue(
                        jsonPath.getFieldName() + ":" + jsonPath.getPath(),
                        jsonPath.getValue(),
                        actualValue));
            }
        }
    }

    public void processSearchResponse(TestCase testCase) throws IOException {
        if(testCase.getAssertions().getSearchResponse() != null) {
            SearchResponse searchResponse = readValue(responseBodyString, SearchResponse.class);
            rawResponseBody = searchResponse;
            for (SearchResponse assertion : testCase.getAssertions().getSearchResponse()) {
                List<AssertionResult> assertionResults = processAssertion(assertion, searchResponse);
                testCaseResult.getAssertionResults().addAll(assertionResults);
            }
        }
    }

    public List<AssertionResult> processAssertion(SearchResponse expected, SearchResponse actual) {
        List<AssertionResult> assertionResults = new ArrayList<>();
        AssertionResult globalStatus = assertGlobalStatus(expected, actual);
        if(globalStatus != null) {
            assertionResults.add(globalStatus);
        }

        if(expected.getResult() != null) {
            if(actual.getResult() == null) {
                assertionResults.add(AssertionResultBuilder.create().
                        name(globalName("no results")).
                        failed().
                        build());
            }
            for(Map.Entry<String, SearchResult> result : expected.getResult().entrySet()) {
                SearchResult actualSearchResult = actual.getResult().get(result.getKey());
                if(actualSearchResult == null) {
                    assertionResults.add(AssertionResultBuilder.create().
                            name(globalName("no result for key " + result.getKey())).
                            failed().
                            build());
                } else {
                    SearchResult expectedSearchResult = result.getValue();
                    List<AssertionResult> assertSearchResults = assertSearchResult(expectedSearchResult, actualSearchResult);
                    assertionResults.addAll(assertSearchResults);
                }

            }
        }

        if(expected.getDidYouMeanResult() != null) {

            if(actual.getDidYouMeanResult() == null || actual.getDidYouMeanResult().getDidYouMean() == null) {
                String message = "Spellcheck expected: " + expected.getDidYouMeanResult().getDidYouMean() + " but was empty.";
                AssertionResult assertionResult = new AssertionResult(message);
                assertionResult.setName(globalName(message));
                assertionResult.setStatus(Status.FAILED);
                assertionResults.add(assertionResult);
            } else if(expected.getDidYouMeanResult().getDidYouMean().equals(actual.getDidYouMeanResult().getDidYouMean())) {
                String message = "Spellcheck expected: " + expected.getDidYouMeanResult().getDidYouMean();
                AssertionResult assertionResult = new AssertionResult(message);
                assertionResult.setName(globalName(message));
                assertionResult.setStatus(Status.SUCCESS);
                assertionResults.add(assertionResult);

            } else {
                String message = "Spellcheck expected: " + expected.getDidYouMeanResult().getDidYouMean() + " but was " + actual.getDidYouMeanResult().getDidYouMean();
                AssertionResult assertionResult = new AssertionResult(message);
                assertionResult.setName(globalName(message));
                assertionResult.setStatus(Status.FAILED);
                assertionResults.add(assertionResult);
            }


        }

        return assertionResults;
    }

    public List<AssertionResult> assertSearchResult(SearchResult expected, SearchResult actual) {
        List<AssertionResult> assertionSearchResults = new ArrayList<>();
        if(expected.getTotal() != null) {
            String message = "Check total hits for " + actual.getName() + " has more than " + expected.getTotal() + " results. actual: " + actual.getTotal();
            AssertionResult assertionResult = new AssertionResult(message);
            assertionResult.setName(globalName("total hits > " + expected.getTotal() + " actual: " + actual.getTotal()));

            if(actual.getTotal() < expected.getTotal()) {
                assertionResult.setStatus(Status.FAILED);

            }
            assertionSearchResults.add(assertionResult);
        }


        if(expected.getDocuments() != null) {
            int pos = 0;
            for(Document document : expected.getDocuments()) {
                List<AssertionResult> assertDocumentResults = assertDocument(document, pos, actual);
                assertionSearchResults.addAll(assertDocumentResults);
                pos++;
            }

        }

        if(expected.getFacets() != null) {
            for(Facet expectedFacet : expected.getFacets()) {
                List<AssertionResult> assertionResults = assertFacets(expectedFacet, actual);
                assertionSearchResults.addAll(assertionResults);
            }
        }
        return assertionSearchResults;
    }

    public List<AssertionResult> assertFacets(Facet expected, SearchResult actual) {
        List<AssertionResult> assertionDocuments = new ArrayList<>();
        Facet actualFacet = actual.getFacetById(expected.getId());

        if(actualFacet == null) {
            String name = "facet " + expected.getId() + " not exists.";
            AssertionResult facetFotExists = AssertionResultBuilder.create().
                    failed().
                    message(name).
                    name(globalName(name)).
                    build();
            assertionDocuments.add(facetFotExists);
        } else {

            String name = "facet " + expected.getId() + " exists.";
            AssertionResult facetFotExists = AssertionResultBuilder.create().
                    success().
                    message(name).
                    name(globalName(name)).
                    build();
            assertionDocuments.add(facetFotExists);

            for(FacetValue expectedValue : expected.getValues()) {
                Optional<FacetValue> actualFacetValue = actualFacet.getValues().stream().
                        filter(f -> f.getValue().equals(expectedValue.getValue())).
                        findFirst();
                if(actualFacetValue.isPresent()) {
                    name = "facet " + expected.getId() + " has value " + expectedValue.getValue();
                    AssertionResult facetSuccess = AssertionResultBuilder.create().
                            success().
                            message(name).
                            name(globalName(name)).
                            build();
                    assertionDocuments.add(facetSuccess);

                    if(expectedValue.getCount() != null && expectedValue.getCount() > actualFacetValue.get().getCount()) {
                        name = "facet count: " + expected.getId() + " value: " + expectedValue.getValue() + " expected: " + expectedValue.getCount() + " <= actual: " + actualFacetValue.get().getCount();
                        AssertionResult countFailed = AssertionResultBuilder.create().
                                failed().
                                message(name).
                                name(globalName(name)).
                                build();
                        assertionDocuments.add(countFailed);

                    } else {
                        name = "facet count: " + expected.getId() + " value: " + expectedValue.getValue() + " expected: " + expectedValue.getCount() + " <= actual: " + actualFacetValue.get().getCount();
                        AssertionResult countSucess = AssertionResultBuilder.create().
                                success().
                                message(name).
                                name(globalName(name)).
                                build();
                        assertionDocuments.add(countSucess);
                    }

                } else {
                    name = "facet " + expected.getId() + " exists but the value " + expectedValue.getValue() + " is not present.";
                    AssertionResult facetSuccess = AssertionResultBuilder.create().
                            failed().
                            message(name).
                            name(globalName(name)).
                            build();
                    assertionDocuments.add(facetSuccess);
                }

            }
        }

        return assertionDocuments;
    }

    public List<AssertionResult> assertDocument(Document expected, Integer expectedPsition, SearchResult actual) {
        List<AssertionResult> assertionDocuments = new ArrayList<>();
        if(expected.getDocument().get("_total") != null) {
            OperatorParser operatorParser = new OperatorParser(expected.getDocument().get("_total"));
            operatorParser.parse();
            String message = "Check total hits for " + actual.getName() + " is " + operatorParser.getOperator().getCode() + " " + operatorParser.getParsedValue().toString() + " results. actual: " + actual.getTotal();
            String name = "total hits " + operatorParser.getOperator().getCode() + " " + operatorParser.getParsedValue() + " actual: " + actual.getTotal();
            if(operatorParser.eval(actual.getTotal())) {
                AssertionResult success = AssertionResultBuilder.create().
                        success().
                        message(message).
                        name(name).
                        build();
                assertionDocuments.add(success);
            } else {
                AssertionResult failed = AssertionResultBuilder.create().
                        failed().
                        message(message).
                        name(name).
                        build();
                assertionDocuments.add(failed);

            }
        }

        String position = null;
        if(expected.getDocument().get("_position") != null) {
            position = expected.getDocument().get("_position").toString();
        }

        if (position == null) {
            List<AssertionResult> assertionResults = assertDocument(expected, actual.getDocuments().get(expectedPsition));
            assertionDocuments.addAll(assertionResults);
        } else if (NumberUtils.isDigits(position)) {
            Integer pos = Integer.valueOf(position);
            if (pos < actual.getDocuments().size()) {
                assertionDocuments.addAll(assertDocument(expected, actual.getDocuments().get(pos)));
            } else {
                // assertion fail
            }
        } else if (position.startsWith("top")) {
            Integer pos = Integer.valueOf(position.replaceAll("top", ""));
            boolean success = false;
            for (int i = 0; i < pos; i++) {
                if (i < actual.getDocuments().size()) {
                    List<AssertionResult> assertionResults = assertDocument(expected, actual.getDocuments().get(i));
                    Status status = getStatus(assertionResults);
                    if (status == Status.SUCCESS) {
                        assertionDocuments.addAll(assertionResults);
                        success = true;
                        break;
                    }
                }
            }
            if (!success) {
                String message = "No " + position + " document matched.";
                AssertionResult assertionResult = new AssertionResult(message);
                assertionResult.setStatus(Status.FAILED);
                assertionResult.setName(globalName(position));
                assertionDocuments.add(assertionResult);
            }

        } else {

        }


        return assertionDocuments;
    }

    public String globalName(String name) {
        return testCase.getName() + " :: " + name + " (" + testCase.getId() + ")";
    }

    Status getStatus(List<AssertionResult> assertionResults) {
        for(AssertionResult assertionResult : assertionResults) {
            if(assertionResult.getStatus() == Status.FAILED) {
                return Status.FAILED;
            }
        }
        return Status.SUCCESS;
    }

    public List<AssertionResult> assertDocument(Document expected, Document actual) {
        List<AssertionResult> assertionDocuments = new ArrayList<>();

        for(Map.Entry<String, Object> entry : expected.getDocument().entrySet()) {

            if(entry.getKey().equals("_jsonPath")) {
                ObjectMapper objectMapper = new ObjectMapper();
                List<JsonPath> jsonPaths = objectMapper.convertValue(expected.getDocument().get("_jsonPath"), new TypeReference<List<JsonPath>>() { });
                for(JsonPath jsonPath : jsonPaths) {
                    Object actualObject = actual.getFieldValueAsObject(jsonPath.getFieldName());
                    String actualValue = JsonPathParser.getValue(jsonPath.getPath(), actualObject);
                    assertionDocuments.addAll(assertStringValue(
                            jsonPath.getFieldName() + ":" + jsonPath.getPath(),
                            jsonPath.getValue(),
                            actualValue));
                }
            }
            if(entry.getKey().startsWith("_") || entry.getKey().startsWith("#")) {
                // ignore iternal fields like _position or comments
                continue;
            }
            if(expected.getValues(entry.getKey()).size() == 1) {
                assertionDocuments.addAll(
                        assertStringValue(
                                entry.getKey(),
                                expected.getFieldValue(entry.getKey()),
                                actual.getFieldValue(entry.getKey())));
            }
        }
        return assertionDocuments;
    }

    public List<AssertionResult>  assertStringValue(String fieldName, String expectedValue, String actualValue) {
        List<AssertionResult> assertionDocuments = new ArrayList<>();
        String operator = "equals";
        String[] splitted = expectedValue.split(Pattern.quote(":="));
        if(splitted.length == 2) {
            operator = splitted[0];
            expectedValue = splitted[1];
        }

        if(actualValue == null) {
            String message = "Check value for field: " + fieldName + " " + operator + " " + expectedValue + " but the value is null in the result.";
            AssertionResult assertionResult = new AssertionResult(message);
            assertionResult.setStatus(Status.FAILED);
            assertionResult.setName(globalName("Field " + fieldName + " not exists"));
            assertionDocuments.add(assertionResult);
        } else if("equals".equals(operator)) {
            String message = "Check value for field: " + fieldName + " equals " + expectedValue;
            AssertionResult assertionResult = new AssertionResult();
            assertionResult.setName(globalName("Field " + fieldName + " equals " + expectedValue));
            if(expectedValue.equals(actualValue)) {
                assertionResult.setStatus(Status.SUCCESS);
                assertionResult.setMessage(message);
            } else {
                assertionResult.setStatus(Status.FAILED);
                assertionResult.setMessage(message + " but was: " + actualValue);
            }
            assertionDocuments.add(assertionResult);
        } else if("startsWith".equals(operator)) {
            String message = "Check value for field: " + fieldName + " startsWith " + expectedValue;
            AssertionResult assertionResult = new AssertionResult();
            assertionResult.setName(globalName("Field " + fieldName + " startsWith " + expectedValue));
            if(actualValue.startsWith(expectedValue)) {
                assertionResult.setStatus(Status.SUCCESS);
                assertionResult.setMessage(message);
            } else {
                assertionResult.setStatus(Status.FAILED);
                assertionResult.setMessage(message + " but was: " + actualValue);
            }
            assertionDocuments.add(assertionResult);
        } else if("startsWithLowerCase".equals(operator)) {
            String message = "Check value for field: " + fieldName + " startsWithLowerCase " + expectedValue.toLowerCase();
            AssertionResult assertionResult = new AssertionResult();
            assertionResult.setName(globalName("Field " + fieldName + " startsWithLowerCase " + expectedValue));
            if(actualValue.toLowerCase().startsWith(expectedValue.toLowerCase())) {
                assertionResult.setStatus(Status.SUCCESS);
                assertionResult.setMessage(message);
            } else {
                assertionResult.setStatus(Status.FAILED);
                assertionResult.setMessage(message + " but was: " + actualValue.toLowerCase());
            }
            assertionDocuments.add(assertionResult);
        } else if("contains".equals(operator)) {
            String message = "Check value for field: " + fieldName + " contains " + expectedValue;
            AssertionResult assertionResult = new AssertionResult();
            assertionResult.setName(globalName("Field " + fieldName + " contains " + expectedValue));
            if(actualValue.contains(expectedValue)) {
                assertionResult.setStatus(Status.SUCCESS);
                assertionResult.setMessage(message);
            } else {
                assertionResult.setStatus(Status.FAILED);
                assertionResult.setMessage(message + " but was: " + actualValue);
            }
            assertionDocuments.add(assertionResult);
        } else if("containsLowerCase".equals(operator)) {
            String message = "Check value for field: " + fieldName + " containsLowerCase " + expectedValue.toLowerCase();
            AssertionResult assertionResult = new AssertionResult();
            assertionResult.setName(globalName("Field " + fieldName + " containsLowerCase " + expectedValue));
            if(actualValue.toLowerCase().contains(expectedValue.toLowerCase())) {
                assertionResult.setStatus(Status.SUCCESS);
                assertionResult.setMessage(message);
            } else {
                assertionResult.setStatus(Status.FAILED);
                assertionResult.setMessage(message + " but was: " + actualValue.toLowerCase());
            }
            assertionDocuments.add(assertionResult);
        } else if("isDateTime".equals(operator)) {
            String message = "Check field: " + fieldName + " is a dateTime value";
            AssertionResult assertionResult = new AssertionResult();
            assertionResult.setName(globalName("Field " + fieldName + " is dateTime value"));

            try {
                DateUtil.getDate(actualValue);
                assertionResult.setStatus(Status.SUCCESS);
                assertionResult.setMessage(message);
            } catch (ParseException e) {
                assertionResult.setStatus(Status.FAILED);
                assertionResult.setMessage(message + " but: " + actualValue + " is not a valid date value.");
            }
            assertionDocuments.add(assertionResult);
        } else if("isBoolean".equals(operator)) {
            String message = "Check field: " + fieldName + " is a boolean value";
            AssertionResult assertionResult = new AssertionResult();
            assertionResult.setName(globalName("Field " + fieldName + " is a boolean value"));

            if("true".equals(actualValue.toLowerCase()) || "false".equals(actualValue.toLowerCase())) {
                assertionResult.setStatus(Status.SUCCESS);
                assertionResult.setMessage(message);
            } else {
                assertionResult.setStatus(Status.FAILED);
                assertionResult.setMessage(message + " but: " + actualValue + " is not a valid boolean value.");
            }
            assertionDocuments.add(assertionResult);
        } else {
            String message = "Check value for field: " + fieldName + " failed. Unknown operator: " + operator;
            AssertionResult assertionResult = new AssertionResult(message);
            assertionResult.setName(globalName("Field " + fieldName + " Unknown operator: " + operator));
            assertionResult.setStatus(Status.FAILED);
            assertionDocuments.add(assertionResult);
        }
        return assertionDocuments;
    }

    public AssertionResult assertGlobalStatus(SearchResponse expected, SearchResponse actual) {
        return assertGlobalStatusCode(expected.getStatusCode(), actual.getStatusCode());
    }

    public AssertionResult assertGlobalStatusCode(Integer expected, Integer actual) {
        if(expected != null) {
            AssertionResult assertionResult = new AssertionResult("Check global status " + actual);
            assertionResult.setName(globalName("Global status " + expected));
            if(!expected.equals(actual)) {
                assertionResult.setStatus(Status.FAILED);
                assertionResult.setMessage("The global status code is: " + actual + " expected: " + expected);
            }
            return assertionResult;
        }
        return null;
    }

    public void execute(HttpUriRequest request) throws IOException {
        long start = System.currentTimeMillis();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(request);
        this.httpResponse = response;
        this.requestTime = System.currentTimeMillis() - start;
        if (response.getStatusLine().getStatusCode() < 300) {
            responseBodyString = EntityUtils.toString(response.getEntity());
        } else {
            StringBuilder responseBuilder = new StringBuilder();
            responseBuilder.append(EntityUtils.toString(response.getEntity()));
            httpclient.close();
            responseBodyString = responseBuilder.toString();


            throw new HttpResponseException(response.getStatusLine().getStatusCode(),
                    " url: " + request.getURI().toString() +
                    " response: " + responseBuilder.toString());
        }
    }

    public <T> T readValue(String content, Class<T> valueType) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(responseBodyString, valueType);
    }

    DocumentContext getResponseDocumentContext() {
        if(responseDocumentContext == null) {
            this.responseDocumentContext = JsonPathParser.getDocumentContext(responseBodyString);
        }
        return responseDocumentContext;

    }
}
