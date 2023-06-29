package com.quasiris.qsf.query;

/**
 * Created by mki on 11.11.16.
 */
public enum FilterDataType {

    STRING("string", String.class),DATE("date", String.class), NUMBER("number", Number.class);

    private final String code;

    private final Class clazz;

    FilterDataType(String code, Class clazz) {
        this.code = code;
        this.clazz = clazz;
    }

    public String getCode() {
        return code;
    }

    /**
     * Getter for property 'clazz'.
     *
     * @return Value for property 'clazz'.
     */
    public Class getClazz() {
        return clazz;
    }

    public boolean isString() {
        return this.equals(STRING);
    }

    public boolean isDate() {
        return this.equals(DATE);
    }

    public boolean isNumber() {
        return this.equals(NUMBER);
    }
}
