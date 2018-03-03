package com.quasiris.qsf.pipeline;

import com.quasiris.qsf.pipeline.filter.SleepFilter;
import com.quasiris.qsf.test.AbstractPipelineTest;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by mki on 28.2.18.
 */
public class ParallelPipelineTest extends AbstractPipelineTest {

    @Test
    public void testParallelPipeline() throws Exception {
        Pipeline pipeline = PipelineBuilder.create().
                pipeline("parallel").
                timeout(10000L).
                parallel().
                pipeline("first-sleep").
                    timeout(2000L).
                    filter(new SleepFilter("first-sleep", 1000L)).
                pipeline("second-sleep").
                    timeout(2000L).
                    filter(new SleepFilter("second-sleep", 1000L)).
                sequential().
                build();

        PipelineContainer pipelineContainer = PipelineExecuter.create().
                pipeline(pipeline).
                execute();

        if(!pipelineContainer.isSuccess()) {
            Assert.fail();
        }

        MatcherAssert.assertThat("currentTime", pipelineContainer.currentTime(), Matchers.lessThan(1100L));
    }

    @Test
    public void testParallelPipelineTimeout() throws Exception {
        Pipeline pipeline = PipelineBuilder.create().
                pipeline("parallel").
                timeout(10000L).
                parallel().
                    pipeline("first-sleep").
                    timeout(2000L).
                filter(new SleepFilter("first-sleep", 1000L)).
                pipeline("second-sleep").
                    timeout(2000L).
                    filter(new SleepFilter("second-sleep",3000L)).
                sequential().
                build();

        PipelineContainer pipelineContainer = PipelineExecuter.create().
                pipeline(pipeline).
                failOnError(false).
                execute();

        Assert.assertFalse(pipelineContainer.isSuccess());
        Assert.assertNotNull("first sleep is available", pipelineContainer.getSearchResult("first-sleep"));
        Assert.assertNull(pipelineContainer.getSearchResult("second-sleep"));

        MatcherAssert.assertThat("currentTime", pipelineContainer.currentTime(), Matchers.lessThan(2100L));
    }



    @Test
    public void testSequentiallPipeline() throws Exception {

        Pipeline pipeline = PipelineBuilder.create().
                pipeline("parallel").
                timeout(10000L).
                filter(new SleepFilter("first" ,1000L)).
                filter(new SleepFilter("second", 1000L)).
                build();

        PipelineContainer pipelineContainer = PipelineExecuter.create().
                pipeline(pipeline).
                execute();

        if(!pipelineContainer.isSuccess()) {
            Assert.fail();
        }

        MatcherAssert.assertThat("currentTime", pipelineContainer.currentTime(), Matchers.greaterThan(1999L));
    }


}
