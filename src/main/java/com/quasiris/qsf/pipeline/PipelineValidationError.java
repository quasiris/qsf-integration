package com.quasiris.qsf.pipeline;

/**
 * Created by mki on 31.12.17.
 */
public class PipelineValidationError {

    public PipelineValidationError(String message) {
        this.message = message;
    }

    private String message;

    public String getMessage() {
        return message;
    }
}
