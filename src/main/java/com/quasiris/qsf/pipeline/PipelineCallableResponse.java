package com.quasiris.qsf.pipeline;

import com.quasiris.qsf.explain.Explain;

/**
 * Created by tbl on 2.4.22.
 */
public class PipelineCallableResponse {

    private Explain explain;
    private PipelineContainer pipelineContainer;

    public PipelineContainer getPipelineContainer() {
        return pipelineContainer;
    }

    public void setPipelineContainer(PipelineContainer pipelineContainer) {
        this.pipelineContainer = pipelineContainer;
    }

    public Explain getExplain() {
        return explain;
    }

    public void setExplain(Explain explain) {
        this.explain = explain;
    }
}
