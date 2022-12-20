package com.quasiris.qsf.pipeline;

import com.quasiris.qsf.explain.Explain;
import com.quasiris.qsf.explain.ExplainContextHolder;
import com.quasiris.qsf.explain.ExplainPipeline;

import java.util.concurrent.Callable;

/**
 * Created by mki on 11.11.17.
 */
public class PipelineCallable implements Callable<PipelineCallableResponse> {

    private Pipeline pipeline;

    private PipelineContainer pipelineContainer;

    private String execLocationId;

    public PipelineCallable(Pipeline pipeline, PipelineContainer pipelineContainer, String execLocationId) {
        this.pipeline = pipeline;
        this.pipelineContainer = pipelineContainer;
        this.execLocationId = execLocationId;
    }

    @Override
    public PipelineCallableResponse call() throws Exception {
        long start = System.currentTimeMillis();
        ExplainContextHolder.clearContext();
        ExplainContextHolder.getContext().setExplain(pipelineContainer.getSearchQuery().isExplain());
        Explain<ExplainPipeline> explain = ExplainContextHolder.getContext().pipeline(pipeline.getId());
        PipelineExecuterService pipelineExecuterService = new PipelineExecuterService(pipeline);
        pipelineExecuterService.setExecLocationId(execLocationId);
        PipelineContainer processedPipelineContainer = pipelineExecuterService.execute(pipelineContainer);
        PipelineCallableResponse response = new PipelineCallableResponse();
        response.setExplain(ExplainContextHolder.getContext().getRoot());
        response.setPipelineContainer(pipelineContainer);
        explain.getExplainObject().setDuration(System.currentTimeMillis() - start);
        return response;
    }
}
