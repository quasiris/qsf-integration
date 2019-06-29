package com.quasiris.qsf.pipeline.filter.qsql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineContainerException;
import com.quasiris.qsf.pipeline.filter.AbstractFilter;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.parser.QsfqlParser;

/**
 * Created by mki on 12.11.17.
 */
public class QSQLRequestFilter extends AbstractFilter {

    private Integer defaultRows;


    public QSQLRequestFilter() {
    }

    public QSQLRequestFilter(int defaultRows) {
        this.defaultRows = defaultRows;
    }

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        if(pipelineContainer.getRequest() != null && "POST".equals(pipelineContainer.getRequest().getMethod())) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                SearchQuery searchQuery = objectMapper.readValue(pipelineContainer.getRequest().getInputStream(), SearchQuery.class);
                searchQuery = setDefaults(searchQuery);
                pipelineContainer.setSearchQuery(searchQuery);
            } catch (Exception e) {
                throw new PipelineContainerException("Could not read convert search query, because: " + e.getMessage() , e);
            }

        } else {
            QsfqlParser qsfqlParser = new QsfqlParser(pipelineContainer.getRequest().getParameterMap());
            SearchQuery searchQuery = qsfqlParser.getQuery();
            searchQuery = setDefaults(searchQuery);
            pipelineContainer.setSearchQuery(searchQuery);
        }
        return pipelineContainer;
    }

    private SearchQuery setDefaults(SearchQuery searchQuery) {
        if(searchQuery.getRows() == null) {
            searchQuery.setRows(defaultRows);
        }
        return searchQuery;
    }
}
