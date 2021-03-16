package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.pipeline.PipelineContainer;

/**
 * Created by mki on 30.12.17.
 */
public class DebugFilter extends AbstractFilter {

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) {
        if(pipelineContainer.getSearchQuery() != null && pipelineContainer.getSearchQuery().isDebug()) {
            pipelineContainer.setDebug(true);
        } else if(pipelineContainer.getRequest() != null && "true".equals(pipelineContainer.getRequest().getParameter("debug"))) {
            pipelineContainer.setDebug(true);
        }
        return pipelineContainer;
    }
}
