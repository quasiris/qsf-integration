package com.quasiris.qsf.test.dto;

import java.util.List;

/**
 * A Testcase represents a query against an API that should fulfill certain assertions for a specific test environment
 */
public class TestCase {

    /**
     * Unique identifier to specify the TestCase within a TestSuite
     */
    private String id;

    /**
     * (optional) Specifies the testing environment in which this TestCase is currently used
     */
    private String env;

    /**
     * (optional) Restricts the TestCase to only run on the specified list of environments specified in TestSuite
     */
    private List<String> envs;

    /**
     *  Human-readable name for the TestCase, used for outputs and logging
     */
    private String name;

    /**
     * (optional) Comment describing what kind of requirements or features the TestCase should cover
     */
    private String comment;

    /**
     * Test query that should be executed for this TestCase
     * @see Query
     */
    private Query query;

    /**
     * (optional) Note that the test is active in a TestSuite (has not function at test execution)
     * TODO implement actual inactivation
     */
    private Boolean active;

    /**
     * Test assertions that should be executed on the query response
     * @see Assertions
     */
    private Assertions assertions;

    /**
     * Getter for property 'id'.
     *
     * @return Value for property 'id'.
     */
    public String getId() {
        return id;
    }

    /**
     * Setter for property 'id'.
     *
     * @param id Value to set for property 'id'.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for property 'comment'.
     *
     * @return Value for property 'comment'.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Setter for property 'comment'.
     *
     * @param comment Value to set for property 'comment'.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Getter for property 'query'.
     *
     * @return Value for property 'query'.
     */
    public Query getQuery() {
        return query;
    }

    /**
     * Setter for property 'query'.
     *
     * @param query Value to set for property 'query'.
     */
    public void setQuery(Query query) {
        this.query = query;
    }

    /**
     * Getter for property 'active'.
     *
     * @return Value for property 'active'.
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * Setter for property 'active'.
     *
     * @param active Value to set for property 'active'.
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Getter for property 'assertions'.
     *
     * @return Value for property 'assertions'.
     */
    public Assertions getAssertions() {
        return assertions;
    }

    /**
     * Setter for property 'assertions'.
     *
     * @param assertions Value to set for property 'assertions'.
     */
    public void setAssertions(Assertions assertions) {
        this.assertions = assertions;
    }

    /**
     * Getter for property 'name'.
     *
     * @return Value for property 'name'.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for property 'name'.
     *
     * @param name Value to set for property 'name'.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for property 'envs'.
     *
     * @return Value for property 'envs'.
     */
    public List<String> getEnvs() {
        return envs;
    }

    /**
     * Setter for property 'envs'.
     *
     * @param envs Value to set for property 'envs'.
     */
    public void setEnvs(List<String> envs) {
        this.envs = envs;
    }

    /**
     * Getter for property 'env'.
     *
     * @return Value for property 'env'.
     */
    public String getEnv() {
        return env;
    }

    /**
     * Setter for property 'env'.
     *
     * @param env Value to set for property 'env'.
     */
    public void setEnv(String env) {
        this.env = env;
    }
}
