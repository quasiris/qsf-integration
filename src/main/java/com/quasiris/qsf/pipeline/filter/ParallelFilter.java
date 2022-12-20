package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.explain.ExplainContextHolder;
import com.quasiris.qsf.pipeline.*;
import com.quasiris.qsf.pipeline.helper.ExecLocationIdHelper;
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
        List<PipelineFutureTask<PipelineCallableResponse>> futureTaskList = new ArrayList<>();
        for (int i = 0; i < pipelines.size(); i++) {
            Pipeline pipeline = pipelines.get(i);
            String curExecId = ExecLocationIdHelper.addIndexAndPipelineId(getExecLocationId(), i, pipeline);
            futureTaskList.add(new PipelineFutureTask<>(new PipelineCallable(pipeline, pipelineContainer, curExecId), pipeline));
        }

        ExecutorService executorService = executorServices.get(executorName);

        for(PipelineFutureTask<PipelineCallableResponse> futureTask :futureTaskList) {
            executorService.execute(futureTask);
        }


        List<PipelineContainer> results = new ArrayList<>();

        for (int i = 0; i < futureTaskList.size(); i++) {
            PipelineFutureTask<PipelineCallableResponse> futureTask = futureTaskList.get(i);
            try {
                LOG.debug("getting result for pipeline " + futureTask.getPipeline().getId());
                PipelineCallableResponse response = futureTask.getWithTimeout();
                ExplainContextHolder.getContext().addChild(response.getExplain());
                PipelineContainer value = response.getPipelineContainer();
                results.add(value);
            } catch (TimeoutException e) {
                Pipeline pipeline = futureTask.getPipeline();
                pipelineContainer.error("The pipeline " + pipeline.getId() + " has not finished in " + pipeline.getTimeout() + " ms.");
                pipelineContainer.error(e);
                String curExecId = ExecLocationIdHelper.addIndexAndPipelineId(getExecLocationId(), i, futureTask.getPipeline());
                pipelineContainer.getPipelineStatus().error(curExecId, "The pipeline " + pipeline.getId() + " has not finished in " + pipeline.getTimeout() + " ms.", e);
                PipelineExecuterService.failOnError(pipelineContainer, e);
            } catch (InterruptedException | ExecutionException e) {
                pipelineContainer.error(e);
                String curExecId = ExecLocationIdHelper.addIndexAndPipelineId(getExecLocationId(), i, futureTask.getPipeline());
                pipelineContainer.getPipelineStatus().error(curExecId, e);
                PipelineExecuterService.failOnError(pipelineContainer, e);
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
