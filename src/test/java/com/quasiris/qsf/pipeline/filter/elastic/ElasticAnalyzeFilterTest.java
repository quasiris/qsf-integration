package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.mock.Mockfactory;
import com.quasiris.qsf.pipeline.Pipeline;
import com.quasiris.qsf.pipeline.PipelineBuilder;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineExecuter;
import com.quasiris.qsf.pipeline.filter.qsql.QSQLRequestFilter;
import com.quasiris.qsf.response.Document;
import com.quasiris.qsf.response.SearchResult;
import com.quasiris.qsf.test.AbstractPipelineTest;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by mki on 11.02.18.
 */
public class ElasticAnalyzeFilterTest extends AbstractPipelineTest {



    @Test
    public void testElasticAnalyzeFilter() throws Exception {
        String baseUrl = "http://localhost:9200/qsf-index/";


        MockElasticClient mockElasticClient = new MockElasticClient();
        //mockElasticClient.setRecord(true);

        ElasticAnalyzeFilter elasticAnalyzeFilter = new ElasticAnalyzeFilter();
        elasticAnalyzeFilter.setBaseUrl(baseUrl);
        elasticAnalyzeFilter.setElasticClient(mockElasticClient);
        elasticAnalyzeFilter.setField("title");
        elasticAnalyzeFilter.setResultSetId("analyze");

        Pipeline pipeline = PipelineBuilder.create().
                pipeline("anazlye").
                timeout(1000L).
                filter(new QSQLRequestFilter()).
                filter(elasticAnalyzeFilter).
                build();

        Assert.assertNotNull(pipeline.print(""));

        HttpServletRequest httpServletRequest = Mockfactory.createHttpServletRequest("http://localhost?q=kündigen");

        PipelineContainer pipelineContainer = PipelineExecuter.create().
                pipeline(pipeline).
                httpRequest(httpServletRequest).
                execute();

        if(!pipelineContainer.isSuccess()) {
            Assert.fail();
        }

        SearchResult analyzeResult = pipelineContainer.getSearchResult("analyze");
        Assert.assertEquals(2, analyzeResult.getDocuments().size());
        Document first = analyzeResult.getDocuments().get(0);

        Assert.assertEquals("beenden", first.getFieldValue("token"));
        Assert.assertEquals(Integer.valueOf(0), first.getFieldValueAsInteger("start_offset"));
        Assert.assertEquals(Integer.valueOf(8), first.getFieldValueAsInteger("end_offset"));
        Assert.assertEquals("SYNONYM", first.getFieldValue("type"));

    }



}
