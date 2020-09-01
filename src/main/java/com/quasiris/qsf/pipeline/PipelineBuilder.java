package com.quasiris.qsf.pipeline;

import com.quasiris.qsf.pipeline.filter.ConditionFilter;
import com.quasiris.qsf.pipeline.filter.Filter;
import com.quasiris.qsf.pipeline.filter.LoopFilter;
import com.quasiris.qsf.pipeline.filter.ParallelFilter;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Created by mki on 11.11.17.
 */
public class PipelineBuilder {

    private PipelineBuilder parent;

    private Pipeline pipeline;

    private ParallelFilter parallelFilter;

    private ConditionFilter conditionFilter;

    private LoopFilter loopFilter;

    public static PipelineBuilder create() {
        return new PipelineBuilder();
    }

    public PipelineBuilder pipeline(String id) throws PipelineContainerException {
        if(this.pipeline == null) {
            pipeline = new Pipeline(id);
            return this;
        }

        if(parent == null) {
            PipelineBuilder pipelineBuilder = new PipelineBuilder();
            pipelineBuilder.setParent(this);
            pipelineBuilder.pipeline(id);
            return pipelineBuilder;
        }

        if(parent.parallelFilter != null) {
            parent.parallelFilter.addPipeline(this.build());
            PipelineBuilder pipelineBuilder = new PipelineBuilder();
            pipelineBuilder.setParent(parent);
            pipelineBuilder.pipeline(id);
            return pipelineBuilder;
        }

        if(parent.conditionFilter != null) {
            parent.conditionFilter.setPipeline(this.build());
            PipelineBuilder pipelineBuilder = new PipelineBuilder();
            pipelineBuilder.setParent(parent);
            pipelineBuilder.pipeline(id);
            return pipelineBuilder;
        }

        if(parent.loopFilter != null) {
            parent.loopFilter.setPipeline(this.build());
            PipelineBuilder pipelineBuilder = new PipelineBuilder();
            pipelineBuilder.setParent(parent);
            pipelineBuilder.pipeline(id);
            return pipelineBuilder;
        }

        throw new RuntimeException("This should never happen.");
    }

    public PipelineBuilder timeout(long timeout) {
        pipeline.setTimeout(timeout);
        return this;
    }

    public PipelineBuilder filter(Filter filter, String id) {
        pipeline.addFilter(filter);
        return this;
    }

    public PipelineBuilder filter(Filter filter) {
        return filter(filter, pipeline.getId());
    }

    public PipelineBuilder conditional(Predicate<PipelineContainer> predicate) {
        conditionFilter = new ConditionFilter(pipeline.getId(), predicate);
        pipeline.addFilter(conditionFilter);
        return this;
    }

    public PipelineBuilder endConditional() throws PipelineContainerException {
        if(parent == null) {
            throw new PipelineContainerException("There is no pipeline defined for the conditional.");
        }
        parent.conditionFilter.setPipeline(this.build());
        return parent;
    }

    public PipelineBuilder loop(Predicate<PipelineContainer> predicate) {
        loopFilter = new LoopFilter(pipeline.getId(), predicate);
        pipeline.addFilter(loopFilter);
        return this;
    }

    public PipelineBuilder endLoop() throws PipelineContainerException {
        if(parent == null) {
            throw new PipelineContainerException("There is no pipeline defined for the loop.");
        }
        parent.loopFilter.setPipeline(this.build());
        return parent;
    }

    public PipelineBuilder parallel() {
        parallelFilter = new ParallelFilter(pipeline.getId());
        pipeline.addFilter(parallelFilter);
        return this;
    }

    public PipelineBuilder parallel(String executorName, int executorSize) {
        parallelFilter = new ParallelFilter(pipeline.getId(), executorName, executorSize);
        pipeline.addFilter(parallelFilter);
        return this;
    }

    public PipelineBuilder endParallel() throws PipelineContainerException {
        return sequential();
    }

    public PipelineBuilder sequential() throws PipelineContainerException {
        parent.parallelFilter.addPipeline(this.build());
        return parent;
    }

    public Pipeline build() throws PipelineContainerException {
        ensureFilterIds();
        validate();
        return pipeline;
    }

    public void validate() throws PipelineContainerException{
        PipelineValidation pipelineValidation = pipeline.validate(new PipelineValidation());
        if(!pipelineValidation.isValid()) {
            StringBuilder errorMessage = new StringBuilder();
            for(PipelineValidationError error : pipelineValidation.getPipelineValidationErrors()) {
                errorMessage.append(error.getMessage()).append("\n");
            }
            throw new PipelineContainerException(errorMessage.toString());
        }
    }

    public void setParent(PipelineBuilder parent) {
        this.parent = parent;
    }


    public void ensureFilterIds() {
        Set<String> alreadyUsedIds = new HashSet<>();

        for(Filter filter : pipeline.getFilterList()) {
            if(filter.getId() == null) {
                String suggestedId = pipeline.getId() + "." + filter.getClass().getSimpleName();
                int counter = 1;
                while(alreadyUsedIds.contains(suggestedId)) {
                    suggestedId = suggestedId + counter++;
                }
                alreadyUsedIds.add(suggestedId);
                filter.setId(suggestedId);
            }
        }

    }
}
