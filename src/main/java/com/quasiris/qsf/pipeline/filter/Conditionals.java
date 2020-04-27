package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.pipeline.PipelineContainer;

import java.util.function.Predicate;

public class Conditionals {


    public static Predicate<PipelineContainer> isNoResult(String resultSetId) {

        return p ->
                p.getSearchResult(resultSetId) != null &&
                p.getSearchResult(resultSetId).getTotal() == 0;
    }
}
