package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.pipeline.PipelineContainer;

/**
 * Created by mki on 30.12.17.
 */
public class DebugFilter extends AbstractFilter {

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) {
        if(pipelineContainer.getRequest() == null) {
            return pipelineContainer;
        }
        if("true".equals(pipelineContainer.getRequest().getParameter("debug"))) {
            pipelineContainer.setDebug(true);
        }
        return pipelineContainer;
    }
}
