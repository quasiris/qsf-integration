package com.quasiris.qsf.pipeline;

/**
 * Created by mki on 23.12.17.
 */
public class PipelineContainerException extends Exception {

    public PipelineContainerException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }

    public PipelineContainerException(String errorMessage) {
        super(errorMessage);
    }
}
