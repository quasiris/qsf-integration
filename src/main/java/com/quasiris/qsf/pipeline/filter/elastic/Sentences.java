package com.quasiris.qsf.pipeline.filter.elastic;

import java.util.ArrayList;
import java.util.List;

public class Sentences {

    private List<String> sentences;



    /**
     * Getter for property 'sentences'.
     *
     * @return Value for property 'sentences'.
     */
    public List<String> getSentences() {
        return sentences;
    }

    /**
     * Setter for property 'sentences'.
     *
     * @param sentences Value to set for property 'sentences'.
     */
    public void setSentences(List<String> sentences) {
        this.sentences = sentences;
    }

    public void addSentence(String sentence) {
        if(this.sentences == null) {
            this.sentences = new ArrayList<>();
        }
        this.sentences.add(sentence);
    }
}
