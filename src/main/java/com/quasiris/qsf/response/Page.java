package com.quasiris.qsf.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by mki on 21.01.18.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Page {

    private Boolean currentPage;

    private Integer number;

    private String parameter;

    public Boolean getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Boolean currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    @Override
    public String toString() {
        return "Page{" +
                "currentPage=" + currentPage +
                ", number=" + number +
                ", parameter='" + parameter + '\'' +
                '}';
    }
}
