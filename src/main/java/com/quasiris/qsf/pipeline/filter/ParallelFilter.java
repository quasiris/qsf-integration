package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.pipeline.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
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

    private int executorSize = 20;

    private static HashMap<String, ExecutorService> executorServices = new HashMap<>();
    private String executorName = "DefaultParallelExecutor";

    public ParallelFilter(String sourcePipelineId) {
        this.sourcePipelineId = sourcePipelineId;
    }

    public ParallelFilter(String sourcePipelineId, String executorName, int executorSize) {
        this.sourcePipelineId = sourcePipelineId;
        this.executorName = executorName;
        this.executorSize = executorSize;
    }

    public ParallelFilter() {
    }

    public void addPipeline(Pipeline pipeline) {
        pipelines.add(pipeline);
    }

    @Override
    public void init() {

        if (executorServices.get(this.executorName) == null) {
            ExecutorService executorService = Executors.newFixedThreadPool(executorSize);
            executorServices.put(this.executorName, executorService);
        }
    }

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        List<PipelineFutureTask<PipelineContainer>> futureTaskList = new ArrayList<>();

        for(Pipeline pipeline: pipelines) {
            futureTaskList.add(new PipelineFutureTask<>(new PipelineCallable(pipeline, pipelineContainer), pipeline));
        }

        ExecutorService executorService = executorServices.get(executorName);

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
                    pipelineContainer.error("The pipeline " + pipeline.getId() + " has not finished in " + pipeline.getTimeout() + " ms.");
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

    /**
     * Getter for property 'pipelines'.
     *
     * @return Value for property 'pipelines'.
     */
    public List<Pipeline> getPipelines() {
        return pipelines;
    }

    public void setPipelines(List<Pipeline> pipelines) {
        this.pipelines = pipelines;
    }

    public void setSourcePipelineId(String sourcePipelineId) {
        this.sourcePipelineId = sourcePipelineId;
    }
}
