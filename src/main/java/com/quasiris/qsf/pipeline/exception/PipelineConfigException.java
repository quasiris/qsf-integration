package com.quasiris.qsf.pipeline.exception;

import com.quasiris.qsf.pipeline.PipelineContainer;

/**
 * Thrown when a pipeline configuration is insufficient or has wrong parameters
 */
public class PipelineConfigException extends PipelineRuntimeException {
    public PipelineConfigException(PipelineContainer pipelineContainer) {
        super(pipelineContainer);
    }

    public PipelineConfigException(PipelineContainer pipelineContainer, String message) {
        super(pipelineContainer, message);
    }

    public PipelineConfigException(PipelineContainer pipelineContainer, String message, Throwable cause) {
        super(pipelineContainer, message, cause);
    }

    public PipelineConfigException(PipelineContainer pipelineContainer, Throwable cause) {
        super(pipelineContainer, cause);
    }

    protected PipelineConfigException(PipelineContainer pipelineContainer, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(pipelineContainer, message, cause, enableSuppression, writableStackTrace);
    }
}
