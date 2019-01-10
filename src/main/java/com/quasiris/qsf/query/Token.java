package com.quasiris.qsf.query;

public class Token {

    private String posTag;

    private String value;

    private String normalizedValue;

    private String attributeName;

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

    @Override
    public String toString() {
        return "Token{" +
                "posTag='" + posTag + '\'' +
                ", value='" + value + '\'' +
                ", normalizedValue='" + normalizedValue + '\'' +
                ", attributeName='" + attributeName + '\'' +
                '}';
    }
}
