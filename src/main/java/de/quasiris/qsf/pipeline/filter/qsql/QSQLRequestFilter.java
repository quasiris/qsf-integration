package de.quasiris.qsf.pipeline.filter.qsql;

import de.quasiris.qsf.pipeline.filter.AbstractFilter;
import de.quasiris.qsf.pipeline.PipelineContainer;
import de.quasiris.qsf.query.SearchQuery;
import de.quasiris.qsf.query.parser.QsfqlParser;

/**
 * Created by mki on 12.11.17.
 */
public class QSQLRequestFilter extends AbstractFilter {

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) {
        QsfqlParser qsfqlParser = new QsfqlParser(pipelineContainer.getRequest().getParameterMap());
        SearchQuery searchQuery = qsfqlParser.getQuery();
        pipelineContainer.setSearchQuery(searchQuery);
        return pipelineContainer;
    }
}
