package com.quasiris.qsf.pipeline.filter;

import com.quasiris.qsf.mock.Mockfactory;
import com.quasiris.qsf.pipeline.Pipeline;
import com.quasiris.qsf.pipeline.PipelineBuilder;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.PipelineExecuter;
import com.quasiris.qsf.pipeline.filter.solr.JsonMonitoringFilter;
import com.quasiris.qsf.response.SearchResult;
import com.quasiris.qsf.test.AbstractPipelineTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by mki on 25.02.18.
 */
public class JsonMonitoringFilterTest extends AbstractPipelineTest {



    @Test
    public void testMonitoring() throws Exception {
        JsonMonitoringFilter solrMonitoringFilter = new JsonMonitoringFilter();
        solrMonitoringFilter.setId("solr");
        solrMonitoringFilter.setUrl("http://localhost:8983/solr/gettingstarted/select?q=*");

        solrMonitoringFilter.addCondition("$.response.numFound", "value > 50");
        solrMonitoringFilter.addCondition("$.responseHeader.status", "value == 52");
        solrMonitoringFilter.setHttpclient(Mockfactory.createCloseableHttpClient("src/test/mock/http/solr-monitoring-response.json", 200));


        Pipeline pipeline = PipelineBuilder.create().
                pipeline("monitoring").
                timeout(4000000L).
                filter(solrMonitoringFilter).
                build();


        PipelineContainer pipelineContainer = PipelineExecuter.create().
                pipeline(pipeline).
                execute();


        SearchResult solrSearchResult = pipelineContainer.getSearchResult("solr");
        Assert.assertEquals(Integer.valueOf(500), solrSearchResult.getStatusCode());
        Assert.assertEquals("200", solrSearchResult.getDocuments().get(0).getFieldValue("status"));
        Assert.assertEquals("500", solrSearchResult.getDocuments().get(1).getFieldValue("status"));
    }
}
