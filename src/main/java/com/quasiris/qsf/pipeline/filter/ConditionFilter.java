package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.pipeline.Pipeline;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineExecuterService;
import com.quasiris.qsf.pipeline.filter.conditions.FilterCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Transient;
import java.util.function.Predicate;

/**
 * Created by tbl on 18.04.20.
 */
public class ConditionFilter extends AbstractFilter {

    private static Logger LOG = LoggerFactory.getLogger(ConditionFilter.class);

    private String sourcePipelineId;

    private FilterCondition condition;

    @Deprecated // use condition
    private Predicate<PipelineContainer> predicate;

    private Pipeline pipeline;

    public ConditionFilter() {
    }

    public ConditionFilter(String sourcePipelineId, FilterCondition condition) {
        this.sourcePipelineId = sourcePipelineId;
        this.setCondition(condition);
    }

    @Deprecated // use condition
    public ConditionFilter(String sourcePipelineId, Predicate<PipelineContainer> predicate) {
        this.sourcePipelineId = sourcePipelineId;
        this.predicate = predicate;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public void init() {
    }

    @Override
    public PipelineContainer filter(PipelineContainer pipelineContainer) throws Exception {
        if(predicate.test(pipelineContainer)) {
            PipelineExecuterService pipelineExecuterService = new PipelineExecuterService(pipeline);
            pipelineContainer = pipelineExecuterService.execute(pipelineContainer);
        }
        return pipelineContainer;
    }

    public FilterCondition getCondition() {
        return condition;
    }

    public void setCondition(FilterCondition condition) {
        this.condition = condition;
        this.predicate = condition.predicate();
    }

    /**
     * Getter for property 'predicate'.
     *
     * @return Value for property 'predicate'.
     */
    @Transient
    public Predicate<PipelineContainer> getPredicate() {
        return predicate;
    }

    /**
     * Setter for property 'predicate'.
     *
     * @param predicate Value to set for property 'predicate'.
     */
    @Transient
    public void setPredicate(Predicate<PipelineContainer> predicate) {
        this.predicate = predicate;
    }

    /**
     * Getter for property 'pipeline'.
     *
     * @return Value for property 'pipeline'.
     */
    public Pipeline getPipeline() {
        return pipeline;
    }
}
