package com.quasiris.qsf.pipeline;

import com.quasiris.qsf.explain.Explain;

/**
 * Created by mki on 23.12.17.
 */
public class PipelineContainerExplainException extends Exception {

    private Explain explain;

    public PipelineContainerExplainException(Explain explain) {
        this.explain = explain;
    }

    public Explain getExplain() {
        return explain;
    }
}
