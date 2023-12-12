package com.quasiris.qsf.test.dto;

import java.util.List;
import java.util.Map;

/**
 * A TestSuite is representing a set of TestCases that should be executed for a specific test environment
 */
public class TestSuite {

    /**
     * The available environments for the TestSuite <br>
     * Each environment has a name as key and an Environment Object as value <br>
     * Environments can contain variables to parameterize the TestCases in the TestSuite
     */
    private Map<String, Environment> env;

    /**
     * The default environment that should be used if a testcase has no environment defined
     */
    private String defaultEnv;

    /**
     * The location where your TestCase files are stored on the filesystem <br>
     * Has to start with `classpath://` in order to reference testcases in the resources dir
     */
    private String location;

    /**
     * The list of TestCases that should be executed in this TestSuite
     */
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
