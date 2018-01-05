package de.quasiris.qsf.pipeline.filter;

import de.quasiris.qsf.pipeline.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 * Created by mki on 04.11.17.
 */
public class ParallelFilter extends AbstractFilter {

    private static Logger LOG = LoggerFactory.getLogger(ParallelFilter.class);


    private List<Pipeline> pipelines = new ArrayList<>();

    private String sourcePipelineId;

    private ExecutorService executorService;

    public ParallelFilter(String sourcePipelineId) {
        this.sourcePipelineId = sourcePipelineId;
    }

    public void addPipeline(Pipeline pipeline) {
        pipelines.add(pipeline);
    }

    @Override
    public void init() {
        if(pipelines.size() > 0) {
            executorService = Executors.newFixedThreadPool(pipelines.size());
        }
    }

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        List<PipelineFutureTask<PipelineContainer>> futureTaskList = new ArrayList<>();

        for(Pipeline pipeline: pipelines) {
            futureTaskList.add(new PipelineFutureTask<>(new PipelineCallable(pipeline, pipelineContainer), pipeline));
        }


        for(PipelineFutureTask<PipelineContainer> futureTask :futureTaskList) {
            executorService.execute(futureTask);
        }


        List<PipelineContainer> results = new ArrayList<>();

            for(PipelineFutureTask<PipelineContainer> futureTask :futureTaskList) {
                try {
                    LOG.debug("getting result for pipeline " + futureTask.getPipeline().getId());
                    PipelineContainer value = futureTask.getWithTimeout();
                    results.add(value);
                } catch (TimeoutException e) {
                    Pipeline pipeline = futureTask.getPipeline();
                    pipelineContainer.error("The pipeline " + pipeline.getId() + " did not finished in " + pipeline.getTimeout() + " ms.");
                    pipelineContainer.error(e);
                    PipelineExecuterService.failOnError(pipelineContainer);
                } catch (InterruptedException | ExecutionException e) {
                    pipelineContainer.error(e);
                    PipelineExecuterService.failOnError(pipelineContainer);
                }
            }


        // TODO merge searchRequest object

        return pipelineContainer;
    }

    @Override
    public StringBuilder print(String indent) {
        StringBuilder printer =  super.print(indent);
        for(Pipeline pipeline: pipelines) {
            printer.append(pipeline.print(indent + "\t").append("\n"));
        }
        return printer;


    }
}
