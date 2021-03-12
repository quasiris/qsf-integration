package com.quasiris.qsf.pipeline.exception;

/**
 * Created by tbl on 12.3.21.
 *
 * Throw this exception to restart a running pipeline.
 *
 * For a restarted pipeline, just the execute method is called again.
 */
public class PipelineRestartException extends Exception {

    private String startPipelineId;


    public PipelineRestartException(String startPipelineId) {
        this.startPipelineId = startPipelineId;
    }

    public String getStartPipelineId() {
        return startPipelineId;
    }

    public void setStartPipelineId(String startPipelineId) {
        this.startPipelineId = startPipelineId;
    }
}
