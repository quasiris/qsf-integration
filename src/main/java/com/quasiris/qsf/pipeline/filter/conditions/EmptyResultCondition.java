package com.quasiris.qsf.pipeline.filter.conditions;

import com.quasiris.qsf.pipeline.PipelineContainer;

import java.util.function.Predicate;

/**
 * Check if resultSet is empty
 */
public class EmptyResultCondition implements FilterCondition {
    private String resultSetId;

    public EmptyResultCondition() {
    }

    public EmptyResultCondition(String resultSetId) {
        this.resultSetId = resultSetId;
    }

    @Override
    public Predicate<PipelineContainer> predicate() {
        return p ->
                p.getSearchResult(resultSetId) != null &&
                        p.getSearchResult(resultSetId).getTotal() == 0;
    }

    public String getResultSetId() {
        return resultSetId;
    }

    public void setResultSetId(String resultSetId) {
        this.resultSetId = resultSetId;
    }
}
