package com.quasiris.qsf.pipeline;

import com.quasiris.qsf.query.SearchQuery;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * Created by mki on 26.12.17.
 */
public class PipelineExecuter {

    private Pipeline pipeline;

    private PipelineContainer pipelineContainer;

    private static HashMap<String, ExecutorService> executorServices = new HashMap<>();
    private String executorName = "DefaultPipelineExecutor";

    private PipelineExecuter() {
        if (executorServices.get(this.executorName) == null) {
            ExecutorService executorService = Executors.newFixedThreadPool(20);
            executorServices.put(this.executorName, executorService);
        }
    }

    public static PipelineExecuter create() {
        return new PipelineExecuter();
    }


    public PipelineExecuter context(String name, Object value) {
        getPipelineContainer().putContext(name, value);
        return this;
    }

    public PipelineExecuter searchQuery(SearchQuery searchQuery) {
        getPipelineContainer().setSearchQuery(searchQuery);
        return this;
    }

    public PipelineExecuter httpRequest(HttpServletRequest httpServletRequest) {
        getPipelineContainer().setRequest(httpServletRequest);
        return this;
    }

    public PipelineExecuter httpResponse(HttpServletResponse httpServletResponse) {
        getPipelineContainer().setResponse(httpServletResponse);
        return this;
    }

    public PipelineExecuter failOnError(boolean failOnError) {
        getPipelineContainer().setFailOnError(failOnError);
        return this;
    }

    public PipelineExecuter debug(boolean debug) {
        getPipelineContainer().setDebug(debug);
        return this;
    }

    public PipelineExecuter enableDebug() {
        getPipelineContainer().enableDebug();
        return this;
    }

    public PipelineExecuter pipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    public PipelineExecuter param(String key, String value) {
        getPipelineContainer().setParameter(key, value);
        return this;
    }

    public PipelineExecuter executor(String executorName, int executorSize) {
        this.executorName = executorName;
        if (executorServices.get(this.executorName) == null) {
            ExecutorService executorService = Executors.newFixedThreadPool(executorSize);
            executorServices.put(this.executorName, executorService);
        }

        return this;
    }

    public PipelineContainer execute() throws PipelineContainerException, PipelineContainerDebugException {
        try {
            ExecutorService executorService = executorServices.get(executorName);
            FutureTask<PipelineContainer> futureTask = new FutureTask<>(new PipelineCallable(pipeline, getPipelineContainer()));
            executorService.execute(futureTask);
            pipelineContainer = futureTask.get(pipeline.getTimeout(), TimeUnit.MILLISECONDS);
            if(pipelineContainer.isDebugEnabled()) {
                throw new PipelineContainerDebugException(pipelineContainer);
            }
        } catch (TimeoutException e) {
            pipelineContainer.error("The pipeline " + pipeline.getId() + " has not finished in " + pipeline.getTimeout() + " ms.");
            pipelineContainer.error(e);
            PipelineExecuterService.failOnError(pipelineContainer);
        } catch (InterruptedException | ExecutionException e) {
            pipelineContainer.error(e);
            PipelineExecuterService.failOnError(pipelineContainer);
        }
        return pipelineContainer;
    }

    public PipelineContainer getPipelineContainer() {
        if(this.pipelineContainer == null) {
            this.pipelineContainer = new PipelineContainer();
        }
        return pipelineContainer;
    }
}
