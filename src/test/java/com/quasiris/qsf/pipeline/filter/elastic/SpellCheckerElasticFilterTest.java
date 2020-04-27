package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.mock.Mockfactory;
import com.quasiris.qsf.pipeline.Pipeline;
import com.quasiris.qsf.pipeline.PipelineBuilder;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineExecuter;
import com.quasiris.qsf.pipeline.filter.ConditionFilter;
import com.quasiris.qsf.pipeline.filter.SpyFilter;
import com.quasiris.qsf.pipeline.filter.TokenizerFilter;
import com.quasiris.qsf.pipeline.filter.qsql.QSQLRequestFilter;
import com.quasiris.qsf.test.AbstractPipelineTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Predicate;

/**
 * Created by mki on 11.02.18.
 */
public class SpellCheckerElasticFilterTest extends AbstractPipelineTest {



    @Test
    @Ignore
    public void testSpellCheckerElasticFilterTest() throws Exception {
        String baseUrl = "http://localhost:9200/dias-spellchecker";


        MockElasticClient mockElasticClient = new MockElasticClient();
        mockElasticClient.setRecord(true);

        SpellCheckElasticFilter spellCheckElasticFilter = new SpellCheckElasticFilter(baseUrl);
        spellCheckElasticFilter.setId("spellchecker");


        ConditionFilter conditionFilter = new ConditionFilter("foo", isCorrectedQuery());
        conditionFilter.setPipeline(
                PipelineBuilder.create().
                pipeline("condition").
                filter(new SpyFilter()).
                build()
        );

        conditionFilter.setPredicate(isCorrectedQuery());

        Pipeline pipeline = PipelineBuilder.create().
                pipeline("anazlye").
                timeout(1000000L).
                filter(new QSQLRequestFilter()).
                filter(new TokenizerFilter()).
                filter(spellCheckElasticFilter).
                filter(conditionFilter).
                build();

        Assert.assertNotNull(pipeline.print(""));

        HttpServletRequest httpServletRequest = Mockfactory.createHttpServletRequest("http://localhost?q=Streaming");

        PipelineContainer pipelineContainer = PipelineExecuter.create().
                pipeline(pipeline).
                httpRequest(httpServletRequest).
                execute();


    }


    public static Predicate<PipelineContainer> isCorrectedQuery() {
        return p -> p.getSearchQuery().getQ().equals(p.getSearchQuery().getOriginalQuery());
    }



}
