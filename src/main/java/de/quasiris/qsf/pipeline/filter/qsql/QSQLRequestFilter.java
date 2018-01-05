package de.quasiris.qsf.pipeline.filter.qsql;

import de.quasiris.qsf.pipeline.filter.AbstractFilter;
import de.quasiris.qsf.pipeline.PipelineContainer;
import de.quasiris.qsf.query.SearchQuery;
import de.quasiris.qsf.query.parser.SaqlParser;

/**
 * Created by mki on 12.11.17.
 */
public class QSQLRequestFilter extends AbstractFilter {

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) {
        SaqlParser saqlParser = new SaqlParser(pipelineContainer.getRequest().getParameterMap());
        SearchQuery searchQuery = saqlParser.getQuery();
        pipelineContainer.setSearchQuery(searchQuery);
        return pipelineContainer;
    }
}
