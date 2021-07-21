package com.quasiris.qsf.test.dto;

import java.util.List;

public class TestCaseResult {

    private String testCaseId;

    private String status;

    private String message;

    private List<AssertionResult> assertionResults;

    /**
     * Getter for property 'testCaseId'.
     *
     * @return Value for property 'testCaseId'.
     */
    public String getTestCaseId() {
        return testCaseId;
    }

    /**
     * Setter for property 'testCaseId'.
     *
     * @param testCaseId Value to set for property 'testCaseId'.
     */
    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }

    /**
     * Getter for property 'status'.
     *
     * @return Value for property 'status'.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Setter for property 'status'.
     *
     * @param status Value to set for property 'status'.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Getter for property 'message'.
     *
     * @return Value for property 'message'.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter for property 'message'.
     *
     * @param message Value to set for property 'message'.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Getter for property 'assertionResults'.
     *
     * @return Value for property 'assertionResults'.
     */
    public List<AssertionResult> getAssertionResults() {
        return assertionResults;
    }

    /**
     * Setter for property 'assertionResults'.
     *
     * @param assertionResults Value to set for property 'assertionResults'.
     */
    public void setAssertionResults(List<AssertionResult> assertionResults) {
        this.assertionResults = assertionResults;
    }
}
