package com.quasiris.qsf.pipeline;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.*;

/**
 * Created by mki on 26.12.17.
 */
public class PipelineExecuter {

    private Pipeline pipeline;

    private PipelineContainer pipelineContainer;

    public static PipelineExecuter create() {
        return new PipelineExecuter();
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

    public PipelineContainer execute() throws PipelineContainerException, PipelineContainerDebugException {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            FutureTask<PipelineContainer> futureTask = new FutureTask<>(new PipelineCallable(pipeline, getPipelineContainer()));
            executorService.execute(futureTask);
            pipelineContainer = futureTask.get(pipeline.getTimeout(), TimeUnit.MILLISECONDS);
            executorService.shutdown();
            if(pipelineContainer.isDebugEnabled()) {
                throw new PipelineContainerDebugException(pipelineContainer);
            }
        } catch (TimeoutException e) {
            pipelineContainer.error("The pipeline " + pipeline.getId() + " did not finished in " + pipeline.getTimeout() + " ms.");
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
