package com.quasiris.qsf.test.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.quasiris.qsf.test.builder.AssertionResultBuilder;
import com.quasiris.qsf.test.dto.AssertionResult;
import com.quasiris.qsf.test.dto.TestCase;
import com.quasiris.qsf.test.dto.TestCaseResult;
import com.quasiris.qsf.test.dto.TestSuite;
import com.quasiris.qsf.util.UrlUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class TestSuiteExecuter {

    private String tenant;
    private String code;

    private TestSuite testSuite;

    List<AssertionResult> assertionResults = new ArrayList<>();

    public TestSuiteExecuter(String tenant, String code) {
        this.tenant = tenant;
        this.code = code;
    }

    public void init() {

        this.testSuite = getTestSuite();
    }

    public void execute() {

        init();

        for(TestCase testCaseId : this.testSuite.getTestCases()) {
            if(testCaseId.getId().startsWith("#")) {
                // comment ... ignore
                continue;
            }

            if(testCaseId.getEnvs() != null && !testCaseId.getEnvs().contains(this.testSuite.getDefaultEnv())) {
                // test is disabled for this env
                continue;
            }
            TestExecuter testExecuter = null;
            try {
                TestCase testCase = getTestCase(testCaseId.getId());
                testCase.setEnv(testSuite.getDefaultEnv());

                if(testSuite.getBaseUrl() != null &&
                        testSuite.getEnv() != null &&
                        testSuite.getDefaultEnv() != null &&
                        testSuite.getEnv().get(testSuite.getDefaultEnv()) != null
                ) {
                    String url = testCase.getQuery().getUrl().replace(testSuite.getBaseUrl(), testSuite.getEnv().get(testSuite.getDefaultEnv()).getBaseUrl());
                    testCase.getQuery().setUrl(url);
                }

                testExecuter = new TestExecuter(testCase);
                TestCaseResult testCaseResult = testExecuter.execute();
                assertionResults.addAll(testCaseResult.getAssertionResults());

                if (testCase.getQuery().getAlternativeQueries() != null) {
                    for (String queryAlternative : testCase.getQuery().getAlternativeQueries()) {
                        TestCase alternativeTestCase = getTestCase(testCaseId.getId());
                        String encoded = UrlUtil.encode(queryAlternative);
                        String newUrl = UrlUtil.replaceQueryParameter(alternativeTestCase.getQuery().getUrl(), "q", encoded);
                        alternativeTestCase.getQuery().setUrl(newUrl);
                        String name = alternativeTestCase.getName() + " " + queryAlternative;
                        alternativeTestCase.setName(name);
                        testExecuter = new TestExecuter(alternativeTestCase);
                        TestCaseResult alternativeTestCaseResult = testExecuter.execute();
                        assertionResults.addAll(alternativeTestCaseResult.getAssertionResults());

                    }
                }
            } catch (Exception e) {
                assertionResults.add(AssertionResultBuilder.create().
                        name(testCaseId.getId()).
                        message(e.getMessage()).
                        failed().
                        build());
            }
        }
    }



    String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }




    TestCase getTestCase(String id) {
        InputStream is = null;
        if(this.testSuite.getLocation().startsWith("classpath://")) {
            String location = this.testSuite.getLocation().replace("classpath://", "/");
            String fileName = location + "/" + id + ".json";
            is = TestSuiteExecuter.class.getResourceAsStream(fileName);
            if(is == null) {
                throw new RuntimeException("Resource filename " + fileName + " not found." );
            }
        }



        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            TestCase testCase  = objectMapper.readValue(is, TestCase.class);
            return testCase;
        } catch (IOException e) {
            throw new RuntimeException("Could not parse test case with id " + id);
        }
    }


    TestSuite getTestSuite() {
        String fileName = "/com/quasiris/qsf/test/testsuite/" + tenant + "/" + code + ".json";
        InputStream is = TestSuiteExecuter.class.getResourceAsStream(fileName);
        if(is == null) {
            throw new RuntimeException("Resource filename " + fileName + " not found." );
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            TestSuite testSuite  = objectMapper.readValue(is, TestSuite.class);
            return testSuite;
        } catch (IOException e) {
            throw new RuntimeException("Could not parse test case with id " + fileName);
        }
    }

    void serializeTestCaseResult(TestCaseResult testCaseResult) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL.NON_NULL);
            objectMapper.writeValue(new File("src/test/resources/com/quasiris/qsf/test/testresults/" + testCaseResult.getTestCaseId()+ ".json"), testCaseResult);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Getter for property 'assertionResults'.
     *
     * @return Value for property 'assertionResults'.
     */
    public List<AssertionResult> getAssertionResults() {
        return assertionResults;
    }
}
