package com.quasiris.qsf.query;

import java.util.List;

public class Token {

    private String posTag;

    private String value;

    private String normalizedValue;

    private String attributeName;

    private List<Integer> phraseTokens;

    private String unit;

    public Token(Token token) {
        this.posTag = token.getPosTag();
        this.value = token.getValue();
        this.normalizedValue = token.getNormalizedValue();
        this.attributeName = token.getAttributeName();
        this.unit = token.getUnit();
    }

    public Token(String value) {
        this.value = value;
    }

    public String getPosTag() {
        return posTag;
    }

    public void setPosTag(String posTag) {
        this.posTag = posTag;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getNormalizedValue() {
        if(normalizedValue == null) {
            return value;
        }
        return normalizedValue;
    }

    public void setNormalizedValue(String normalizedValue) {
        this.normalizedValue = normalizedValue;
    }

    public List<Integer> getPhraseTokens() {
        return phraseTokens;
    }

    public void setPhraseTokens(List<Integer> phraseTokens) {
        this.phraseTokens = phraseTokens;
    }

    public boolean isPhrase() {
        return phraseTokens != null;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "Token{" +
                "posTag='" + posTag + '\'' +
                ", value='" + value + '\'' +
                ", normalizedValue='" + normalizedValue + '\'' +
                ", attributeName='" + attributeName + '\'' +
                ", unit='" + unit + '\'' +
                '}';
    }
}
