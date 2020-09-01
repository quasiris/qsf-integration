package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.pipeline.Pipeline;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineExecuterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

/**
 * Created by tbl on 1.09.20.
 */
public class LoopFilter extends AbstractFilter {

    private static Logger LOG = LoggerFactory.getLogger(LoopFilter.class);

    private int maxLoops = 10;

    private long maxTime = 10000;

    private String sourcePipelineId;

    private Predicate<PipelineContainer> predicate;

    private Pipeline pipeline;

    public LoopFilter(String sourcePipelineId, Predicate<PipelineContainer> predicate) {
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
        int loopCount = 0;
        long loopTime = 0;
        long start = System.currentTimeMillis();
        while(predicate.test(pipelineContainer) && loopCount <= maxLoops && loopTime < maxTime) {
            PipelineExecuterService pipelineExecuterService = new PipelineExecuterService(pipeline);
            pipelineContainer = pipelineExecuterService.execute(pipelineContainer);
            loopCount++;
            loopTime = System.currentTimeMillis() - start;
        }
        return pipelineContainer;
    }

    /**
     * Getter for property 'predicate'.
     *
     * @return Value for property 'predicate'.
     */
    public Predicate<PipelineContainer> getPredicate() {
        return predicate;
    }

    /**
     * Setter for property 'predicate'.
     *
     * @param predicate Value to set for property 'predicate'.
     */
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

    /**
     * Getter for property 'maxLoops'.
     *
     * @return Value for property 'maxLoops'.
     */
    public int getMaxLoops() {
        return maxLoops;
    }

    /**
     * Setter for property 'maxLoops'.
     *
     * @param maxLoops Value to set for property 'maxLoops'.
     */
    public void setMaxLoops(int maxLoops) {
        this.maxLoops = maxLoops;
    }

    /**
     * Getter for property 'maxTime'.
     *
     * @return Value for property 'maxTime'.
     */
    public long getMaxTime() {
        return maxTime;
    }

    /**
     * Setter for property 'maxTime'.
     *
     * @param maxTime Value to set for property 'maxTime'.
     */
    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }
}
