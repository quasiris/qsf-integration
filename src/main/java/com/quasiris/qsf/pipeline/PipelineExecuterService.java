package com.quasiris.qsf.pipeline;

import com.quasiris.qsf.pipeline.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mki on 11.11.17.
 */
public class PipelineExecuterService {

    private static Logger LOG = LoggerFactory.getLogger(PipelineExecuterService.class);

    private Pipeline pipeline;

    public PipelineExecuterService(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public PipelineContainer execute(PipelineContainer pipelineContainer) throws PipelineContainerException {

        pipelineContainer.start();
        long start = System.currentTimeMillis();

        for(Filter filter : pipeline.getFilterList()) {
            filter.init();
        }


        for(Filter filter : pipeline.getFilterList()) {
            failOnError(pipelineContainer);
            try {
                LOG.debug("The filter: " + filter.getId() + " started.");
                filter.start();
                if(filter.isActive() && pipelineContainer.isFilterActive(filter.getId())) {
                    pipelineContainer = filter.filter(pipelineContainer);
                } else {
                    LOG.debug("The filter: " + filter.getId() + " is not active.");
                }
                LOG.debug("The filter: " + filter.getId() + " took: " + filter.getCurrentTime() + " ms.");
            } catch (Exception e) {
                LOG.debug("The filter: " + filter.getId() + " failed with an error: " + e.getMessage());
                filter.onError(pipelineContainer, e);
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

    public static void failOnError(PipelineContainer pipelineContainer) throws PipelineContainerException {
        if(pipelineContainer.isFailOnError() && !pipelineContainer.isSuccess()) {
            throw new PipelineContainerException(pipelineContainer.getMessage());
        }
    }

}
