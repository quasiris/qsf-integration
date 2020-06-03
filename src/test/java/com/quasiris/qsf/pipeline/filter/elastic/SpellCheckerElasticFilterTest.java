package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.mock.Mockfactory;
import com.quasiris.qsf.pipeline.Pipeline;
import com.quasiris.qsf.pipeline.PipelineBuilder;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineExecuter;
import com.quasiris.qsf.pipeline.filter.TokenizerFilter;
import com.quasiris.qsf.pipeline.filter.qsql.QSQLRequestFilter;
import com.quasiris.qsf.test.AbstractPipelineTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by tbl on 11.4.20.
 */
public class SpellCheckerElasticFilterTest extends AbstractPipelineTest {

    @Test
    public void testSpellCheckerElasticFilterTest() throws Exception {
        String baseUrl = "http://localhost:9200/dias-spellchecker-dev";


        MockMultiElasticClient mockMultiElasticClient = new MockMultiElasticClient();
        //mockMultiElasticClient.setRecord(true);

        SpellCheckElasticFilter spellCheckElasticFilter = new SpellCheckElasticFilter(baseUrl);
        spellCheckElasticFilter.setId("spellchecker");
        spellCheckElasticFilter.setElasticClient(mockMultiElasticClient);
        spellCheckElasticFilter.setSentenceScoringEnabled(false);


        Pipeline pipeline = PipelineBuilder.create().
                pipeline("spell-checker-test").
                timeout(100000L).
                filter(new QSQLRequestFilter()).
                filter(new TokenizerFilter()).
                filter(spellCheckElasticFilter).
                build();

        Assert.assertNotNull(pipeline.print(""));

        HttpServletRequest httpServletRequest = Mockfactory.createHttpServletRequest("http://localhost?q=Mgenta");

        PipelineContainer pipelineContainer = PipelineExecuter.create().
                pipeline(pipeline).
                httpRequest(httpServletRequest).
                execute();

        Assert.assertEquals("magenta", pipelineContainer.getSearchQuery().getQ());
        Assert.assertEquals("Mgenta", pipelineContainer.getSearchQuery().getOriginalQuery());
        Assert.assertTrue(pipelineContainer.getSearchQuery().isQueryChanged());

    }



}
