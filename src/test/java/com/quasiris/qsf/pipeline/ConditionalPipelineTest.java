package com.quasiris.qsf.pipeline;

import com.quasiris.qsf.pipeline.filter.ConditionFilter;
import com.quasiris.qsf.pipeline.filter.SleepFilter;
import com.quasiris.qsf.pipeline.filter.UnitTestingFilter;
import com.quasiris.qsf.test.AbstractPipelineTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Predicate;

/**
 * Created by mki on 18.4.20.
 */
public class ConditionalPipelineTest extends AbstractPipelineTest {

    @Test
    public void testConditionalPipelineTrue() throws Exception {
        Pipeline pipeline = PipelineBuilder.create().
                pipeline("test-pipeline").
                timeout(10000L).
                filter(new SleepFilter("first-sleep", 10L)).
                conditional(alwaysTrue()).
                    pipeline("conditional-pipeline").
                    filter(new UnitTestingFilter("conditional-filter")).
                endConditional().
                build();


        ConditionFilter conditionFilter = (ConditionFilter) pipeline.getFilterList().get(1);
        Assert.assertEquals("first-sleep", pipeline.getFilterList().get(0).getId());
        Assert.assertEquals("conditional-pipeline", conditionFilter.getPipeline().getId());

        UnitTestingFilter unitTestingFilter = (UnitTestingFilter) conditionFilter.getPipeline().getFilterList().get(0);
        Assert.assertEquals("conditional-filter", unitTestingFilter.getId());

        PipelineContainer pipelineContainer = PipelineExecuter.create().
                pipeline(pipeline).
                execute();

        if(!pipelineContainer.isSuccess()) {
            Assert.fail();
        }

        Assert.assertNotNull(pipelineContainer.getSearchResult("conditional-filter"));


    }

    @Test
    public void testConditionalPipelineFalse() throws Exception {
        Pipeline pipeline = PipelineBuilder.create().
                pipeline("test-pipeline").
                timeout(10000L).
                filter(new SleepFilter("first-sleep", 10L)).
                conditional(alwaysFalse()).
                    pipeline("conditional-pipeline").
                    filter(new UnitTestingFilter("conditional-filter")).
                endConditional().
                build();


        ConditionFilter conditionFilter = (ConditionFilter) pipeline.getFilterList().get(1);
        Assert.assertEquals("first-sleep", pipeline.getFilterList().get(0).getId());
        Assert.assertEquals("conditional-pipeline", conditionFilter.getPipeline().getId());

        UnitTestingFilter unitTestingFilter = (UnitTestingFilter) conditionFilter.getPipeline().getFilterList().get(0);
        Assert.assertEquals("conditional-filter", unitTestingFilter.getId());

        PipelineContainer pipelineContainer = PipelineExecuter.create().
                pipeline(pipeline).
                execute();

        if(!pipelineContainer.isSuccess()) {
            Assert.fail();
        }

        Assert.assertNull(pipelineContainer.getSearchResult("conditional-filter"));


    }



    public static Predicate<PipelineContainer> alwaysTrue() {
        return p -> true;
    }

    public static Predicate<PipelineContainer> alwaysFalse() {
        return p -> false;
    }



}