package com.quasiris.qsf.pipeline.filter.conditions;

import com.quasiris.qsf.pipeline.PipelineContainer;

import java.util.function.Predicate;

/**
 * Wrapper for predicate serialization
 */
public interface FilterCondition {
    Predicate<PipelineContainer> predicate();
}
