package com.quasiris.qsf.test.dto;

import java.util.List;
import java.util.Map;

public class TestSuite {

    private Map<String, Environment> env;

    private String defaultEnv;
    private String location;
    private List<TestCase> testCases;

    /**
     * Getter for property 'location'.
     *
     * @return Value for property 'location'.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Setter for property 'location'.
     *
     * @param location Value to set for property 'location'.
     */
    public void setLocation(String location) {
        this.location = location;
    }


    /**
     * Getter for property 'env'.
     *
     * @return Value for property 'env'.
     */
    public Map<String, Environment> getEnv() {
        return env;
    }

    /**
     * Setter for property 'env'.
     *
     * @param env Value to set for property 'env'.
     */
    public void setEnv(Map<String, Environment> env) {
        this.env = env;
    }

    /**
     * Getter for property 'defaultEnv'.
     *
     * @return Value for property 'defaultEnv'.
     */
    public String getDefaultEnv() {
        return defaultEnv;
    }

    /**
     * Setter for property 'defaultEnv'.
     *
     * @param defaultEnv Value to set for property 'defaultEnv'.
     */
    public void setDefaultEnv(String defaultEnv) {
        this.defaultEnv = defaultEnv;
    }

    /**
     * Getter for property 'testCases'.
     *
     * @return Value for property 'testCases'.
     */
    public List<TestCase> getTestCases() {
        return testCases;
    }

    /**
     * Setter for property 'testCases'.
     *
     * @param testCases Value to set for property 'testCases'.
     */
    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }
}
