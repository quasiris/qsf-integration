package com.quasiris.qsf.explain;

public class ExplainPipelineAutoClosable implements AutoCloseable {
    private final Explain<ExplainPipeline> pipelineExplain;

    public ExplainPipelineAutoClosable(Explain<ExplainPipeline> pipelineExplain) {
        this.pipelineExplain = pipelineExplain;
    }

    @Override
    public void close() {
        ExplainContext context = ExplainContextHolder.getContext();
        if (context.isExplain()){
            context.setCurrent(context.getRoot());
        }
    }

    public Explain<ExplainPipeline> getPipelineExplain() {
        return pipelineExplain;
    }
}
