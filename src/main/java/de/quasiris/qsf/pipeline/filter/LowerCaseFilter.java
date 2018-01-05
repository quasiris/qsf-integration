package de.quasiris.qsf.pipeline.filter;

import de.quasiris.qsf.pipeline.PipelineContainer;
import de.quasiris.qsf.query.SearchQuery;

/**
 * Created by mki on 04.11.17.
 */
public class LowerCaseFilter extends AbstractFilter {

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) {
        SearchQuery searchRequest = pipelineContainer.getSearchQuery();
        String q = searchRequest.getQ();
        searchRequest.setQ(q.toLowerCase());
        return pipelineContainer;
    }
}
