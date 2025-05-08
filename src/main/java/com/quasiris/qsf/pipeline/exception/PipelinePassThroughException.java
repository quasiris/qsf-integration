package com.quasiris.qsf.pipeline.exception;

import com.quasiris.qsf.pipeline.PipelineContainer;

public class PipelinePassThroughException extends RuntimeException {
    private final PipelineContainer pipelineContainer;

    public PipelinePassThroughException(PipelineContainer pipelineContainer, Throwable cause) {
        super(cause);
        this.pipelineContainer = pipelineContainer;
    }

    public PipelineContainer getPipelineContainer() {
        return pipelineContainer;
    }
}
