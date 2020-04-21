package com.quasiris.qsf.pipeline.filter.elastic.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by tbl on 16.04.20.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyzeToken {

    private String token;
    private Integer start_offset;
    private Integer end_offset;
    private String type;
    private Integer position;
    private Integer positionLength;

    /**
     * Getter for property 'token'.
     *
     * @return Value for property 'token'.
     */
    public String getToken() {
        return token;
    }

    /**
     * Setter for property 'token'.
     *
     * @param token Value to set for property 'token'.
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Getter for property 'start_offset'.
     *
     * @return Value for property 'start_offset'.
     */
    public Integer getStart_offset() {
        return start_offset;
    }

    /**
     * Setter for property 'start_offset'.
     *
     * @param start_offset Value to set for property 'start_offset'.
     */
    public void setStart_offset(Integer start_offset) {
        this.start_offset = start_offset;
    }

    /**
     * Getter for property 'end_offset'.
     *
     * @return Value for property 'end_offset'.
     */
    public Integer getEnd_offset() {
        return end_offset;
    }

    /**
     * Setter for property 'end_offset'.
     *
     * @param end_offset Value to set for property 'end_offset'.
     */
    public void setEnd_offset(Integer end_offset) {
        this.end_offset = end_offset;
    }

    /**
     * Getter for property 'type'.
     *
     * @return Value for property 'type'.
     */
    public String getType() {
        return type;
    }

    /**
     * Setter for property 'type'.
     *
     * @param type Value to set for property 'type'.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Getter for property 'position'.
     *
     * @return Value for property 'position'.
     */
    public Integer getPosition() {
        return position;
    }

    /**
     * Setter for property 'position'.
     *
     * @param position Value to set for property 'position'.
     */
    public void setPosition(Integer position) {
        this.position = position;
    }

    /**
     * Getter for property 'positionLength'.
     *
     * @return Value for property 'positionLength'.
     */
    public Integer getPositionLength() {
        return positionLength;
    }

    /**
     * Setter for property 'positionLength'.
     *
     * @param positionLength Value to set for property 'positionLength'.
     */
    public void setPositionLength(Integer positionLength) {
        this.positionLength = positionLength;
    }

    @Override
    public String toString() {
        return "AnalyzeToken{" +
                "token='" + token + '\'' +
                ", start_offset=" + start_offset +
                ", end_offset=" + end_offset +
                ", type='" + type + '\'' +
                ", position=" + position +
                ", positionLength=" + positionLength +
                '}';
    }
}
