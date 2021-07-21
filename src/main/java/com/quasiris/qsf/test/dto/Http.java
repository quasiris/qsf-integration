package com.quasiris.qsf.test.dto;

import java.util.List;

public class Http {

    private String statusCode;
    private List<HttpHeader> header;

    /**
     * Getter for property 'statusCode'.
     *
     * @return Value for property 'statusCode'.
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * Setter for property 'statusCode'.
     *
     * @param statusCode Value to set for property 'statusCode'.
     */
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Getter for property 'header'.
     *
     * @return Value for property 'header'.
     */
    public List<HttpHeader> getHeader() {
        return header;
    }

    /**
     * Setter for property 'header'.
     *
     * @param header Value to set for property 'header'.
     */
    public void setHeader(List<HttpHeader> header) {
        this.header = header;
    }
}
