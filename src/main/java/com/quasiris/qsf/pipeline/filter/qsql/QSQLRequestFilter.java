package com.quasiris.qsf.pipeline.filter.qsql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.parser.QsfqlParser;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.pipeline.PipelineContainer;

/**
 * Created by mki on 12.11.17.
 */
public class QSQLRequestFilter extends AbstractFilter {

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        if("POST".equals(pipelineContainer.getRequest().getMethod())) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                SearchQuery searchQuery = objectMapper.readValue(pipelineContainer.getRequest().getInputStream(), SearchQuery.class);
                pipelineContainer.setSearchQuery(searchQuery);
            } catch (Exception e) {
                throw new PipelineContainerException("Could not read convert search query, becaouse: " + e.getMessage() , e);
            }

        } else {
            QsfqlParser qsfqlParser = new QsfqlParser(pipelineContainer.getRequest().getParameterMap());
            SearchQuery searchQuery = qsfqlParser.getQuery();
            pipelineContainer.setSearchQuery(searchQuery);
        }
        return pipelineContainer;
    }
}
