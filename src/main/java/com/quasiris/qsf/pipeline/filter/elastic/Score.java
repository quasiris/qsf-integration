package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.pipeline.filter.elastic.spellcheck.SpellCheckTokenType;

import java.util.List;

public class Score {


    public Score() {
    }

    private String text;

    private Double score = 0.0;

    private List<SpellCheckTokenType> types;

    public List<SpellCheckTokenType> getTypes() {
        return types;
    }

    public void setTypes(List<SpellCheckTokenType> types) {
        this.types = types;
    }


    public Score(String text, Double score) {
        this.text = text.trim();
        this.score = score;
    }

    /**
     * Getter for property 'text'.
     *
     * @return Value for property 'text'.
     */
    public String getText() {
        return text;
    }

    /**
     * Setter for property 'text'.
     *
     * @param text Value to set for property 'text'.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Getter for property 'score'.
     *
     * @return Value for property 'score'.
     */
    public Double getScore() {
        return score;
    }

    /**
     * Setter for property 'score'.
     *
     * @param score Value to set for property 'score'.
     */
    public void setScore(Double score) {
        this.score = score;
    }
}
