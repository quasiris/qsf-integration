package com.quasiris.qsf.test.dto;

import java.util.List;

public class TestResult {

    private Integer total;
    private Integer failed;

    private List<TestCaseResult> testResults;

    /**
     * Getter for property 'total'.
     *
     * @return Value for property 'total'.
     */
    public Integer getTotal() {
        return total;
    }

    /**
     * Setter for property 'total'.
     *
     * @param total Value to set for property 'total'.
     */
    public void setTotal(Integer total) {
        this.total = total;
    }

    /**
     * Getter for property 'failed'.
     *
     * @return Value for property 'failed'.
     */
    public Integer getFailed() {
        return failed;
    }

    /**
     * Setter for property 'failed'.
     *
     * @param failed Value to set for property 'failed'.
     */
    public void setFailed(Integer failed) {
        this.failed = failed;
    }

    /**
     * Getter for property 'testResults'.
     *
     * @return Value for property 'testResults'.
     */
    public List<TestCaseResult> getTestResults() {
        return testResults;
    }

    /**
     * Setter for property 'testResults'.
     *
     * @param testResults Value to set for property 'testResults'.
     */
    public void setTestResults(List<TestCaseResult> testResults) {
        this.testResults = testResults;
    }
}
