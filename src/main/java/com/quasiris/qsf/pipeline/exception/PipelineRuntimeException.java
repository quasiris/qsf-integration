package com.quasiris.qsf.pipeline.exception;

import com.quasiris.qsf.pipeline.PipelineContainer;

/**
 * Thrown on any RuntimeException occurring in the pipeline
 */
public class PipelineRuntimeException extends RuntimeException {
    private PipelineContainer pipelineContainer;

    public PipelineRuntimeException(PipelineContainer pipelineContainer) {
        super();
        this.pipelineContainer = pipelineContainer;
    }

    public PipelineRuntimeException(PipelineContainer pipelineContainer, String message) {
        super(message);
        this.pipelineContainer = pipelineContainer;
    }

    public PipelineRuntimeException(PipelineContainer pipelineContainer, String message, Throwable cause) {
        super(message, cause);
        this.pipelineContainer = pipelineContainer;
    }

    public PipelineRuntimeException(PipelineContainer pipelineContainer, Throwable cause) {
        super(cause);
        this.pipelineContainer = pipelineContainer;
    }

    protected PipelineRuntimeException(PipelineContainer pipelineContainer, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.pipelineContainer = pipelineContainer;
    }
}
