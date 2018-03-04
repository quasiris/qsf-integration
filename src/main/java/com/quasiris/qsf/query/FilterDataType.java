package com.quasiris.qsf.query;

import java.util.Date;

/**
 * Created by mki on 11.11.16.
 */
public enum FilterDataType {

    STRING("string", String.class),DATE("date", Date.class), NUMBER("number", Number.class);

    private final String code;

    private final Class clazz;

    FilterDataType(String code, Class clazz) {
        this.code = code;
        this.clazz = clazz;
    }

    public String getCode() {
        return code;
    }
}
