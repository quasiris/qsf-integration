package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.mock.Mockfactory;
import com.quasiris.qsf.pipeline.Pipeline;
import com.quasiris.qsf.pipeline.PipelineBuilder;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineExecuter;
import com.quasiris.qsf.pipeline.filter.TokenizerFilter;
import com.quasiris.qsf.pipeline.filter.qsql.QSQLRequestFilter;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.test.AbstractPipelineTest;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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

        assertNotNull(pipeline.print(""));

        HttpServletRequest httpServletRequest = Mockfactory.createHttpServletRequest("http://localhost?q=Mgenta");

        PipelineContainer pipelineContainer = PipelineExecuter.create().
                pipeline(pipeline).
                httpRequest(httpServletRequest).
                execute();

        SearchQuery searchQuery = pipelineContainer.getSearchQuery();
        assertEquals("magenta", searchQuery.getQ());
        assertEquals("Mgenta", searchQuery.getOriginalQuery());
        assertThat("spellcheck", is(in(searchQuery.getQueryChangedReasons())));

        assertTrue(pipelineContainer.getSearchQuery().isQueryChanged());

    }



}
