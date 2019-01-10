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
    SYM("<SYM>"),
    UNKNOWN("<UNKNOWN>"),
    TODO("<TODO>"),
    IGNORE("<IGNORE>"),
    NUM("<NUM>");

    private String value;

    PosTag(String value) {
        this.value = value;
    }

    public boolean isValue(String value) {
        return value.equals(this.value);
    }
}
