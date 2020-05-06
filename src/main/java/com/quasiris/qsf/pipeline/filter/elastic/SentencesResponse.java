package com.quasiris.qsf.pipeline.filter.elastic;

import java.util.List;

public class SentencesResponse {

    private List<Score> sentences;

    /**
     * Getter for property 'sentences'.
     *
     * @return Value for property 'sentences'.
     */
    public List<Score> getSentences() {
        return sentences;
    }

    /**
     * Setter for property 'sentences'.
     *
     * @param sentences Value to set for property 'sentences'.
     */
    public void setSentences(List<Score> sentences) {
        this.sentences = sentences;
    }
}
