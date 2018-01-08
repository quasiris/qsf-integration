package com.quasiris.qsf.pipeline;

/**
 * Created by mki on 23.12.17.
 */
public class PipelineContainerException extends Exception {

    private String errorMessage;

    public PipelineContainerException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
