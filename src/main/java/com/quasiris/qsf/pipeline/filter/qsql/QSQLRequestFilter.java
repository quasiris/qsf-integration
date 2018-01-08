package com.quasiris.qsf.pipeline.filter.qsql;

import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.parser.QsfqlParser;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.pipeline.PipelineContainer;

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
