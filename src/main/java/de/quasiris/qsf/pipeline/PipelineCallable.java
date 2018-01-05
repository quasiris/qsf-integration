package de.quasiris.qsf.pipeline;

import java.util.concurrent.Callable;

/**
 * Created by mki on 11.11.17.
 */
public class PipelineCallable implements Callable<PipelineContainer> {

    private Pipeline pipeline;

    private PipelineContainer pipelineContainer;

    public PipelineCallable(Pipeline pipeline, PipelineContainer pipelineContainer) {
        this.pipeline = pipeline;
        this.pipelineContainer = pipelineContainer;
    }

    @Override
    public PipelineContainer call() throws Exception {
        PipelineExecuterService pipelineExecuterService = new PipelineExecuterService(pipeline);
        PipelineContainer processedPipelineContainer = pipelineExecuterService.execute(pipelineContainer);
        return processedPipelineContainer;
    }
}
