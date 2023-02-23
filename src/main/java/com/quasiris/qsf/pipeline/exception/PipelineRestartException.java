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

    private String startPipelineId;

    private PipelineContainer pipelineContainer;


    public PipelineRestartException(String startPipelineId, PipelineContainer pipelineContainer) {
        this.startPipelineId = startPipelineId;
        this.pipelineContainer = pipelineContainer;
    }

    public String getStartPipelineId() {
        return startPipelineId;
    }

    public void setStartPipelineId(String startPipelineId) {
        this.startPipelineId = startPipelineId;
    }

    public PipelineContainer getPipelineContainer() {
        return pipelineContainer;
    }

    public void setPipelineContainer(PipelineContainer pipelineContainer) {
        this.pipelineContainer = pipelineContainer;
    }
}
