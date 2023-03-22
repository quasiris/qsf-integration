package com.quasiris.qsf.pipeline;

import com.quasiris.qsf.explain.*;

import java.util.concurrent.Callable;

/**
 * Created by mki on 11.11.17.
 */
public class PipelineCallable implements Callable<PipelineCallableResponse> {

    private Pipeline pipeline;

    private PipelineContainer pipelineContainer;

    private ExplainContext parentContext;

    public PipelineCallable(Pipeline pipeline, PipelineContainer pipelineContainer, ExplainContext parentContext) {
        this.pipeline = pipeline;
        this.pipelineContainer = pipelineContainer;
        this.parentContext = parentContext;
    }

    @Override
    public PipelineCallableResponse call() throws Exception {
        long start = System.currentTimeMillis();
        ExplainContextHolder.getContext().setClearOnNewPipeline(parentContext.isClearOnNewPipeline());
        ExplainContextHolder.clearContext();
        ExplainContextHolder.getContext().setExplain(pipelineContainer.getSearchQuery().isExplain());
        PipelineCallableResponse response;
        try (ExplainPipelineAutoClosable explainPipelineAutoClosable = ExplainContextHolder.getContext().pipeline(pipeline.getId())) {
            parentContext.addChild(explainPipelineAutoClosable.getPipelineExplain());
            PipelineExecuterService pipelineExecuterService = new PipelineExecuterService(pipeline);
            PipelineContainer processedPipelineContainer = pipelineExecuterService.execute(pipelineContainer);
            response = new PipelineCallableResponse();
            response.setExplain(ExplainContextHolder.getContext().getRoot());
            response.setPipelineContainer(pipelineContainer);
            explainPipelineAutoClosable.getPipelineExplain().getExplainObject().setDuration(System.currentTimeMillis() - start);
        }
        return response;
    }
}
