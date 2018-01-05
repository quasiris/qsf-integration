package de.quasiris.qsf.pipeline.filter;

import de.quasiris.qsf.pipeline.PipelineContainer;
import de.quasiris.qsf.query.SearchQuery;

/**
 * Created by mki on 11.11.17.
 */
public class UpperCaseFilter extends AbstractFilter {

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) {
        SearchQuery searchRequest = pipelineContainer.getSearchQuery();
        String q = searchRequest.getQ();
        searchRequest.setQ(q.toUpperCase());
        return pipelineContainer;
    }
}
