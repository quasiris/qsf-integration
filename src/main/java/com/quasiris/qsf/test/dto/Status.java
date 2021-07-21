package com.quasiris.qsf.test.dto;

public enum Status {

    FAILED("failed"),SUCCESS("success");


    private final String code;

    Status(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
