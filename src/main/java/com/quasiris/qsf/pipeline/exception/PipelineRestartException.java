package com.quasiris.qsf.pipeline.exception;

import com.quasiris.qsf.pipeline.PipelineContainer;

/**
 * Created by tbl on 12.3.21.
 *
 * Throw this exception to restart a running pipeline.
 *
 * For a restarted pipeline, just the execute method is called again.
 */
public class PipelineRestartException extends Exception {

    private PipelineContainer pipelineContainer;

    public PipelineRestartException(PipelineContainer pipelineContainer, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.pipelineContainer = pipelineContainer;
    }

    public PipelineRestartException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }

    public PipelineRestartException(String errorMessage) {
        super(errorMessage);
    }

    public PipelineRestartException(PipelineContainer pipelineContainer, String errorMessage) {
        super(errorMessage);
        this.pipelineContainer = pipelineContainer;
    }

    public PipelineContainer getPipelineContainer() {
        return pipelineContainer;
    }
}
