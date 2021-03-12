package com.quasiris.qsf.pipeline.exception;

import com.quasiris.qsf.pipeline.PipelineContainer;

/**
 * Created by tbl on 8.3.21.
 *
 * Throw this exception to stop a running pipeline.
 *
 * For a stopped pipeline, the init and end methods are still executed.
 */
public class PipelineStopException extends Exception {

    private PipelineContainer pipelineContainer;

    public PipelineStopException(PipelineContainer pipelineContainer, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.pipelineContainer = pipelineContainer;
    }

    public PipelineStopException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }

    public PipelineStopException(String errorMessage) {
        super(errorMessage);
    }

    public PipelineStopException(PipelineContainer pipelineContainer, String errorMessage) {
        super(errorMessage);
        this.pipelineContainer = pipelineContainer;
    }

    public PipelineContainer getPipelineContainer() {
        return pipelineContainer;
    }
}
