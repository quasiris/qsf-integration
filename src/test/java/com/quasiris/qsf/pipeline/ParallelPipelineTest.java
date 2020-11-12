package com.quasiris.qsf.pipeline;

import com.quasiris.qsf.pipeline.filter.ParallelFilter;
import com.quasiris.qsf.pipeline.filter.SleepFilter;
import com.quasiris.qsf.test.AbstractPipelineTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
                pipeline("third-sleep").
                    timeout(2000L).
                    filter(new SleepFilter("third-sleep", 1000L)).
                sequential().
                build();


        ParallelFilter parallelFilter = (ParallelFilter) pipeline.getFilterList().get(0);
        assertEquals("parallel.ParallelFilter", pipeline.getFilterList().get(0).getId());
        assertEquals("first-sleep", parallelFilter.getPipelines().get(0).getId());
        assertEquals("second-sleep", parallelFilter.getPipelines().get(1).getId());
        assertEquals("third-sleep", parallelFilter.getPipelines().get(2).getId());

        PipelineContainer pipelineContainer = PipelineExecuter.create().
                pipeline(pipeline).
                execute();

        if(!pipelineContainer.isSuccess()) {
            fail();
        }

        assertThat("currentTime", pipelineContainer.currentTime(), Matchers.lessThan(1100L));
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

        assertFalse(pipelineContainer.isSuccess());
        assertNotNull(pipelineContainer.getSearchResult("first-sleep"), "first sleep is available");
        assertNull(pipelineContainer.getSearchResult("second-sleep"));

        assertThat("currentTime", pipelineContainer.currentTime(), Matchers.lessThan(2100L));
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
            fail();
        }

        assertThat("currentTime", pipelineContainer.currentTime(), Matchers.greaterThan(1999L));
    }


}
