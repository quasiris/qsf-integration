package com.quasiris.qsf.pipeline;

import java.util.List;

/**
 * Created by mki on 23.12.17.
 */
public class PipelineContainerDebugException extends Exception {

    private PipelineContainer pipelineContainer;

    public PipelineContainerDebugException(PipelineContainer pipelineContainer) {
        this.pipelineContainer = pipelineContainer;
    }

    public List<Object> getDebugStack() {
        return this.pipelineContainer.getDebugStack();
    }
}
