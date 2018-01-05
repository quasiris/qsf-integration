package de.quasiris.qsf.pipeline.filter;

import de.quasiris.qsf.pipeline.PipelineContainer;
import de.quasiris.qsf.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mki on 23.12.17.
 */
public class SpyFilter extends AbstractFilter {

    private static Logger LOG = LoggerFactory.getLogger(SpyFilter.class);

    private String name;

    public SpyFilter() {
    }

    public SpyFilter(String name) {
        this.name = name;
    }

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) {
        LOG.info("spyFilter " + name + " searchResults: " + JsonUtil.toPrettyString(pipelineContainer.getSearchResults()));
        LOG.info("spyFilter " + name + " searchQuery: " + JsonUtil.toPrettyString(pipelineContainer.getSearchQuery()));
        return pipelineContainer;
    }
}
