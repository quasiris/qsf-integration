package com.quasiris.qsf.mapping;

public class ParameterMappingDTO {

    private String from;
    private String to;

    public ParameterMappingDTO() {
    }

    public ParameterMappingDTO(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
