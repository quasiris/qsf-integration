package com.quasiris.qsf.pipeline.filter.elastic.suggest;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

public class SuggestContext {


    private List<String> suggestFields;
    private Set<String> startTokens;
    private StringJoiner startTokenJoiner;

    /**
     * Getter for property 'suggestFields'.
     *
     * @return Value for property 'suggestFields'.
     */
    public List<String> getSuggestFields() {
        return suggestFields;
    }

    /**
     * Setter for property 'suggestFields'.
     *
     * @param suggestFields Value to set for property 'suggestFields'.
     */
    public void setSuggestFields(List<String> suggestFields) {
        this.suggestFields = suggestFields;
    }

    /**
     * Getter for property 'startTokens'.
     *
     * @return Value for property 'startTokens'.
     */
    public Set<String> getStartTokens() {
        return startTokens;
    }

    /**
     * Setter for property 'startTokens'.
     *
     * @param startTokens Value to set for property 'startTokens'.
     */
    public void setStartTokens(Set<String> startTokens) {
        this.startTokens = startTokens;
    }

    /**
     * Getter for property 'startTokenJoiner'.
     *
     * @return Value for property 'startTokenJoiner'.
     */
    public StringJoiner getStartTokenJoiner() {
        return startTokenJoiner;
    }

    /**
     * Setter for property 'startTokenJoiner'.
     *
     * @param startTokenJoiner Value to set for property 'startTokenJoiner'.
     */
    public void setStartTokenJoiner(StringJoiner startTokenJoiner) {
        this.startTokenJoiner = startTokenJoiner;
    }
}
