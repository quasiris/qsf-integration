package com.quasiris.qsf.test.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quasiris.qsf.test.dto.AssertionResult;
import com.quasiris.qsf.test.dto.Status;
import com.quasiris.qsf.test.dto.TestCaseResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestSuiteExecuterTest {
    @Test
    void executeEmptySuite() {
        TestSuiteExecuter testSuiteExecuter = new TestSuiteExecuter("mytenant", "empty");
        testSuiteExecuter.execute();
    }

    @Test
    void executeUnparsableSuite() {
        TestSuiteExecuter testSuiteExecuter = new TestSuiteExecuter("mytenant", "unparsable");
        Assertions.assertThrows(RuntimeException.class, testSuiteExecuter::execute);
    }

    @Test
    void executeNotExistsSuite() {
        TestSuiteExecuter testSuiteExecuter = new TestSuiteExecuter("mytenant", "not-exists");
        Assertions.assertThrows(RuntimeException.class, testSuiteExecuter::execute);
    }

    @Test
    void executeSuccess() throws JsonProcessingException {
        TestCaseResult testCaseResult = mock(TestCaseResult.class);
        AssertionResult assertResult = mock(AssertionResult.class);
        try (MockedConstruction<TestExecuter> mockConstruct = Mockito.mockConstruction
                (TestExecuter.class, (testExecuter, context) -> when(testExecuter.execute()).thenReturn(testCaseResult))) {
            when(testCaseResult.getAssertionResults()).thenReturn(Collections.singletonList(assertResult));
            TestSuiteExecuter testSuiteExecuter = new TestSuiteExecuter("mytenant", "success");
            testSuiteExecuter.execute();
            List<AssertionResult> assertionResults = testSuiteExecuter.getAssertionResults();
            Assertions.assertEquals(1, assertionResults.size());
            Assertions.assertSame(assertResult, assertionResults.get(0));
        }
    }

    @Test
    void executeWithVariationsSuccess() throws JsonProcessingException {
        TestCaseResult testCaseResult = mock(TestCaseResult.class);
        AssertionResult assertResult = mock(AssertionResult.class);
        try (MockedConstruction<TestExecuter> mockConstruct = Mockito.mockConstruction
                (TestExecuter.class, (testExecuter, context) -> when(testExecuter.execute()).thenReturn(testCaseResult))) {
            when(testCaseResult.getAssertionResults()).thenReturn(Collections.singletonList(assertResult));
            TestSuiteExecuter testSuiteExecuter = new TestSuiteExecuter("mytenant", "variations");
            testSuiteExecuter.execute();
            List<AssertionResult> assertionResults = testSuiteExecuter.getAssertionResults();
            Assertions.assertEquals(1, assertionResults.size());
            Assertions.assertSame(assertResult, assertionResults.get(0));
        }
    }


    @Test
    void executeWithVariationsExceptionAssertFailed() throws JsonProcessingException {
        TestCaseResult testCaseResult = mock(TestCaseResult.class);
        AssertionResult assertResult = mock(AssertionResult.class);
        try (MockedConstruction<TestExecuter> mockConstruct = Mockito.mockConstruction
                (TestExecuter.class, (testExecuter, context) -> when(testExecuter.execute()).thenThrow(RuntimeException.class))) {
            when(testCaseResult.getAssertionResults()).thenReturn(Collections.singletonList(assertResult));
            TestSuiteExecuter testSuiteExecuter = new TestSuiteExecuter("mytenant", "variations");
            testSuiteExecuter.execute();
            List<AssertionResult> assertionResults = testSuiteExecuter.getAssertionResults();
            Assertions.assertEquals(1, assertionResults.size());
            AssertionResult actual = assertionResults.get(0);
            Assertions.assertEquals(Status.FAILED, actual.getStatus());
            Assertions.assertEquals("withVariations",actual.getName());
        }
    }
}