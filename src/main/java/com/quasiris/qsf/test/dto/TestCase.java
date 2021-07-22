package com.quasiris.qsf.test.dto;

import java.util.List;

public class TestCase {

    private String id;

    private List<String> envs;

    private String name;

    private String comment;

    private Query query;

    private Boolean active;

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
}
