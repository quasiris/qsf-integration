package com.quasiris.qsf.pipeline;

import com.quasiris.qsf.commons.util.YamlFactory;
import com.quasiris.qsf.explain.Explain;
import com.quasiris.qsf.explain.ExplainContextHolder;
import com.quasiris.qsf.explain.ExplainPipelineAutoClosable;
import com.quasiris.qsf.pipeline.exception.PipelineRestartException;
import com.quasiris.qsf.query.SearchQuery;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;
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
    public PipelineExecuter contextMerge(Map<String, ? super Object> context ) {
        if(context != null) {
            for (Map.Entry<String, ? super Object> entry : context.entrySet()) {
                getPipelineContainer().putContextIfNotExists(entry.getKey(), entry.getValue());
            }
        }
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

    public PipelineContainer execute() throws PipelineContainerException, PipelineContainerDebugException, PipelineRestartException {
        try {
            ExplainContextHolder.clearContext();
            if(pipelineContainer != null) {
                ExplainContextHolder.getContext().setExplain(pipelineContainer.getSearchQuery().isExplain());
            }
            try (ExplainPipelineAutoClosable explainPipelineAutoClosable = ExplainContextHolder.getContext().pipeline(pipeline.getId())) {
                ExecutorService executorService = executorServices.get(executorName);
                FutureTask<PipelineCallableResponse> futureTask = new FutureTask<>(new PipelineCallable(pipeline, getPipelineContainer(), ExplainContextHolder.getContext()));
                executorService.execute(futureTask);
                PipelineCallableResponse response = futureTask.get(pipeline.getTimeout(), TimeUnit.MILLISECONDS);
                pipelineContainer = response.getPipelineContainer();

                if(ExplainContextHolder.getContext().isExplain()) {
                    String debugPipeline = YamlFactory.serialize(pipeline);
                    Explain debugPipelineExplain = ExplainContextHolder.getContext().createExplainJson("pipeline", debugPipeline);
                    explainPipelineAutoClosable.getPipelineExplain().getChildren().add(debugPipelineExplain);
                }
                explainPipelineAutoClosable.getPipelineExplain().getExplainObject().setDuration(pipelineContainer.currentTime());
            }
            if(pipelineContainer.isDebugEnabled()) {
                throw new PipelineContainerDebugException(pipelineContainer);
            }
        } catch (TimeoutException e) {
            pipelineContainer.error("The pipeline " + pipeline.getId() + " has not finished in " + pipeline.getTimeout() + " ms.");
            pipelineContainer.error(e);
            PipelineExecuterService.failOnError(pipelineContainer);
        } catch (InterruptedException | ExecutionException e) {
            if(e.getCause() instanceof PipelineRestartException) {
                throw (PipelineRestartException) e.getCause();
            }
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
