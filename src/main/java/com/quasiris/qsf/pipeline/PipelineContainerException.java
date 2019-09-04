package com.quasiris.qsf.pipeline;

/**
 * Created by mki on 23.12.17.
 */
public class PipelineContainerException extends Exception {

    private PipelineContainer pipelineContainer;

    public PipelineContainerException(PipelineContainer pipelineContainer, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.pipelineContainer = pipelineContainer;
    }

    public PipelineContainerException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }

    public PipelineContainerException(String errorMessage) {
        super(errorMessage);
    }

    public PipelineContainerException(PipelineContainer pipelineContainer, String errorMessage) {
        super(errorMessage);
        this.pipelineContainer = pipelineContainer;
    }

    public PipelineContainer getPipelineContainer() {
        return pipelineContainer;
    }
}
