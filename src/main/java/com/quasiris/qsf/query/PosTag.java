package com.quasiris.qsf.query;

public enum PosTag {

    ATTR("<ATTR>"),
    ATTRV("<ATTRV>"),
    PRODUCT("<PRODUCT>"),
    BRAND("<BRAND>"),
    AND("<AND>"),
    OR("<OR>"),
    LESS("<LESS>"),
    GREATER("<GREATER>"),
    BETWEEN("<BETWEEN>"),
    UNIT("<UNIT>"),
    NOT("<NOT>"),
    SYM("<SYM>"),
    UNKNOWN("<UNKNOWN>"),
    TODO("<TODO>"),
    IGNORE("<IGNORE>"),
    NUM("<NUM>"),
    PWAV("<PWAV>"),
    VAFIN("<VAFIN>"),
    ADJD("<ADJD>"),
    APPRART("<APPRART>"),
    NN("<NN>"),
    APPR("<APPR>"),
    NE("<NE>"),
    CARD("<CARD>"),
    XY("<XY>"),
    KON("<KON>"),
    ADV("<ADV>"),
    KOKOM("<KOKOM>"),
    VVFIN("<VVFIN>"),
    PIAT("<PIAT>"),
    PIS("<PIS>"),
    PWAT("<PWAT>"),
    ADJA("<ADJA>"),
    ART("<ART>"),
    PIDAT("<PIDAT>"),
    MAX("<MAX>"),
    MIN("<MIN>")
    ;

    private String value;

    PosTag(String value) {
        this.value = value;
    }

    public boolean isValue(String value) {
        return value.equals(this.value);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static boolean isOneOfValue(String value, PosTag... posTags) {
        for(PosTag posTag : posTags) {
            boolean isValue = posTag.isValue(value);
            if(isValue) {
                return true;
            }
        }
        return false;

    }
}
