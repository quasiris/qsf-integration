package de.quasiris.qsf.pipeline.filter;

import de.quasiris.qsf.pipeline.PipelineContainer;

/**
 * Created by mki on 30.12.17.
 */
public class DebugFilter extends AbstractFilter {

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) {
        if("true".equals(pipelineContainer.getRequest().getParameter("debug"))) {
            pipelineContainer.setDebug(true);
        }
        return pipelineContainer;
    }
}
