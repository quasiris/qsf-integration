package com.quasiris.qsf.pipeline;

import com.quasiris.qsf.exception.Debug;
import com.quasiris.qsf.explain.Explain;
import com.quasiris.qsf.explain.ExplainContextHolder;
import com.quasiris.qsf.pipeline.exception.PipelineRestartException;
import com.quasiris.qsf.pipeline.exception.PipelineStopException;
import com.quasiris.qsf.pipeline.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mki on 11.11.17.
 */
public class PipelineExecuterService {

    private static Logger LOG = LoggerFactory.getLogger(PipelineExecuterService.class);

    private Pipeline pipeline;

    public PipelineExecuterService(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public PipelineContainer execute(PipelineContainer pipelineContainer) throws PipelineContainerException, PipelineRestartException {

        pipelineContainer.start();
        long start = System.currentTimeMillis();

        for(Filter filter : pipeline.getFilterList()) {
            filter.init();
        }

        int restartCount = 0;
        while(restartCount < 2) {
            try {
                pipelineContainer = filter(pipelineContainer);
                restartCount = 1000;
            } catch (PipelineRestartException e) {
                if(e.getStartPipelineId().equals(pipeline.getId())) {
                    restartCount++;
                } else {
                    throw e;
                }
            }
        }

        failOnError(pipelineContainer);

        for(Filter filter : pipeline.getFilterList()) {
            filter.end();
        }
        long took = System.currentTimeMillis() - start;
        LOG.debug("The pipeline: " + pipeline.getId() + " took: " + took + " ms.");
        return pipelineContainer;
    }

    private PipelineContainer filter(PipelineContainer pipelineContainer) throws PipelineContainerException, PipelineRestartException {
        for(Filter filter : pipeline.getFilterList()) {
            Explain currentExplain = ExplainContextHolder.getContext().getCurrent();
            Explain filterExplain = ExplainContextHolder.getContext().filter(filter.getId());
            failOnError(pipelineContainer);
            try {
                LOG.debug("The filter: " + filter.getId() + " started.");
                filter.start();
                if(filter.isActive() && pipelineContainer.isFilterActive(filter.getId())) {
                    pipelineContainer = filter.filter(pipelineContainer);
                } else {
                    LOG.debug("The filter: " + filter.getId() + " is not active.");
                }
                if(pipelineContainer.isDebugEnabled()) {
                    debugRuntime(pipelineContainer, filter);
                }
                LOG.debug("The filter: " + filter.getId() + " took: " + filter.getCurrentTime() + " ms.");
            } catch(PipelineStopException stop)  {
                LOG.debug("The filter: " + filter.getId() + " was stopped.");
                if(pipelineContainer.isDebugEnabled()) {
                    debugRuntime(pipelineContainer, filter);
                }
                ExplainContextHolder.getContext().setCurrent(currentExplain);
                ExplainContextHolder.getContext().explain("stopPipeline", "pipeline.stop", "pipeline.stop");

                return pipelineContainer;
            } catch(PipelineRestartException restart)  {
                LOG.debug("The filter: " + filter.getId() + " initiated a restart of the pipeline.");
                if(pipelineContainer.isDebugEnabled()) {
                    debugRuntime(pipelineContainer, filter);
                }
                ExplainContextHolder.getContext().setCurrent(currentExplain);
                ExplainContextHolder.getContext().explain("restartPipeline", "restart." + restart.getStartPipelineId(), "restart pipeline " + restart.getStartPipelineId());
                throw restart;
            } catch (Exception e) {
                LOG.debug("The filter: " + filter.getId() + " failed with an error: " + e.getMessage());
                if(pipelineContainer.isDebugEnabled()) {
                    debugRuntime(pipelineContainer, filter);
                }
                filter.onError(pipelineContainer, e);
                ExplainContextHolder.getContext().explain("exception", filter.getId() + ".error", e.getMessage());
            }
            filterExplain.setDuration(filter.getCurrentTime());
            ExplainContextHolder.getContext().setCurrent(currentExplain);
        }
        return pipelineContainer;
    }

    private void debugRuntime(PipelineContainer pipelineContainer, Filter filter) {
        Debug debug = pipelineContainer.getDebugStack().stream().
                filter(d -> "_runtime".equals(d.getId())).
                findFirst().
                orElse(null);
        if(debug == null) {
            debug = new Debug();
            debug.setId("_runtime");
            debug.setDebugObject(new ArrayList<String>());
            pipelineContainer.debug(debug);
        }

        List<String> runtime = (List<String>) debug.getDebugObject();
        runtime.add(filter.getId() + " took: " + filter.getCurrentTime() + " ms.");
    }

    public static void failOnError(PipelineContainer pipelineContainer) throws PipelineContainerException {
        if(pipelineContainer.isFailOnError() && !pipelineContainer.isSuccess()) {
            throw new PipelineContainerException(pipelineContainer, pipelineContainer.getMessage());
        }
    }

}
