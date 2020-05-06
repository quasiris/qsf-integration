package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.query.Token;

import java.util.List;

public class SpellCheckToken {

    public SpellCheckToken(Token token) {
        this.token = token;
    }

    private Token token;

    private Integer elasticResultPojnter;

    private List<Score> correctedVariants;

    private boolean unknownToken = false;

    private boolean correctToken = true;


    /**
     * Getter for property 'correctToken'.
     *
     * @return Value for property 'correctToken'.
     */
    public boolean isCorrectToken() {
        return correctToken;
    }

    /**
     * Setter for property 'correctToken'.
     *
     * @param correctToken Value to set for property 'correctToken'.
     */
    public void setCorrectToken(boolean correctToken) {
        this.correctToken = correctToken;
    }

    /**
     * Getter for property 'unknownToken'.
     *
     * @return Value for property 'unknownToken'.
     */
    public boolean isUnknownToken() {
        return unknownToken;
    }

    /**
     * Setter for property 'unknownToken'.
     *
     * @param unknownToken Value to set for property 'unknownToken'.
     */
    public void setUnknownToken(boolean unknownToken) {
        this.unknownToken = unknownToken;
    }

    /**
     * Getter for property 'token'.
     *
     * @return Value for property 'token'.
     */
    public Token getToken() {
        return token;
    }

    /**
     * Setter for property 'token'.
     *
     * @param token Value to set for property 'token'.
     */
    public void setToken(Token token) {
        this.token = token;
    }

    /**
     * Getter for property 'elasticResultPojnter'.
     *
     * @return Value for property 'elasticResultPojnter'.
     */
    public Integer getElasticResultPojnter() {
        return elasticResultPojnter;
    }

    /**
     * Setter for property 'elasticResultPojnter'.
     *
     * @param elasticResultPojnter Value to set for property 'elasticResultPojnter'.
     */
    public void setElasticResultPojnter(Integer elasticResultPojnter) {
        this.elasticResultPojnter = elasticResultPojnter;
    }

    /**
     * Getter for property 'correctedVariants'.
     *
     * @return Value for property 'correctedVariants'.
     */
    public List<Score> getCorrectedVariants() {
        return correctedVariants;
    }

    /**
     * Setter for property 'correctedVariants'.
     *
     * @param correctedVariants Value to set for property 'correctedVariants'.
     */
    public void setCorrectedVariants(List<Score> correctedVariants) {
        this.correctedVariants = correctedVariants;
    }
}
