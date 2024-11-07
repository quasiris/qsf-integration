package com.quasiris.qsf.config;

public class QsfSearchConfigDTO {

    private Object sort;
    private Object filter;
    private Object facets;
    private Object paging;
    private Object variant;

    // profiles with conditions
    private Object profiles;

    private DisplayDTO display;


    public DisplayDTO getDisplay() {
        return display;
    }

    public void setDisplay(DisplayDTO display) {
        this.display = display;
    }
}
